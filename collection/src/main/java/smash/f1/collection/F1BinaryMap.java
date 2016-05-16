/*
 * Copyright 2016 Smash.bi Inc.
 * http://www.smash.bi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package smash.f1.collection;

import java.io.IOException;

import smash.f1.core.agrona.LongAtomicBuffer;
import smash.f1.core.agrona.LongDirectBuffer;
import smash.f1.core.agrona.LongMutableDirectBuffer;

/**
 * F1BinaryMap is a small foot print off heap binary fixed length key value data lookup
 * that is backed by various options of memory media such as memory mapped file or direct off heap memory. 
 * F1BinaryMap can handle very large binary data efficiently.
 * 
 * The binary region of the map is separated into
 * 3 regions - Map Header Region, Hash Bucket Region, and Record Region.
 * 
 * Map Header Region consists of 9 long fields (72 bytes) with starting offset 0
 * LOCK protects storage allocation operations
 * SBRK size of sbrk area in recordRegion, i.e., index of last usable record
 * END last unused record in sbrk area, i.e., exceeds sbrk => increase sbrk
 * FREE stack (LIFO) of free recordRegion, chained together by hash-chain link
 * RECORD SIZE size of each record
 * NO OF FILES current no of memory mapped file backing the map
 * NO OF BUCKETS current no of buckets in the map
 * MAX MAP SIZE maximum size of the map in bytes
 * SIZE current no of records in the map
 * 
 * Hash Bucket Region consists of no of buckets of 2 long fields (2 bytes * no of buckets) with offset 72
 * TOP address to the first record in the bucket
 * LOCK protects bucket record allocation/deallocation operations
 * 
 * Record Region consists of 1 long field, followed by the key and then the value with offset 72 + 16 * no of buckets
 * LINK long field to store the address of the linked record node
 * KEY key of the data
 * VALUE value of the data
 */
public class F1BinaryMap 
{    
    private final static int WORDSIZE = Long.SIZE / Byte.SIZE;
    private final static int LINK_VALUE_SIZE = WORDSIZE;
    private final static int RECORD_KEY_OFFSET = LINK_VALUE_SIZE;
    public final static long NULL = -1;			// JNI null pointer
      
	// Map Header constants
    private final static long	MAP_HEADER_BASE_OFFSET = 0;
    private final static long	MAP_HEADER_NO_OF_FIELDS = 9;
    // protects storage allocation operations
    private final static long	MAP_HEADER_FIELD_ADDRESS_LOCK = GetLongAddress( MAP_HEADER_BASE_OFFSET, 0 );
    // size of sbrk area in recordRegion, i.e., index of last usable record
    private final static long	MAP_HEADER_FIELD_ADDRESS_SBRK= GetLongAddress( MAP_HEADER_BASE_OFFSET, 1 );
    // last unused record in sbrk area, i.e., exceeds sbrk => increase sbrk
    private final static long	MAP_HEADER_FIELD_ADDRESS_END = GetLongAddress( MAP_HEADER_BASE_OFFSET, 2 );
    // stack (LIFO) of free recordRegion, chained together by hash-chain link
    private final static long	MAP_HEADER_FIELD_ADDRESS_FREE = GetLongAddress( MAP_HEADER_BASE_OFFSET, 3 );
    // size of record in fields
    private final static long	MAP_HEADER_FIELD_ADDRESS_RECORD_SIZE = GetLongAddress( MAP_HEADER_BASE_OFFSET, 4 );
    // number of files composing mapped file
    private final static long	MAP_HEADER_FIELD_ADDRESS_NO_OF_MEMORY_PAGES = GetLongAddress( MAP_HEADER_BASE_OFFSET, 5 );
    // number of buckets in hash table
    private final static long	MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS = GetLongAddress( MAP_HEADER_BASE_OFFSET, 6 );
    // maximum size (GB), after which evicts begin
    private final static long	MAP_HEADER_FIELD_ADDRESS_MAX_MAP_SIZE = GetLongAddress( MAP_HEADER_BASE_OFFSET, 7 );
    // size of the map no of records in the map
    private final static long	MAP_HEADER_FIELD_ADDRESS_SIZE = GetLongAddress( MAP_HEADER_BASE_OFFSET, 8 );
    private final static long	VALUE_UNLOCKED = 0;
    private final static long	VALUE_LOCKED = 1;
    
    // Hash Buckets constants
    private final static long	HASH_BUCKETS_BASE_OFFSET = MAP_HEADER_BASE_OFFSET + MAP_HEADER_NO_OF_FIELDS * WORDSIZE;
    private final static long	HASH_BUCKETS_NO_OF_FIELDS = 2; // Top and Lock
    private final static long	HASH_BUCKETS_STRIDE = HASH_BUCKETS_NO_OF_FIELDS * WORDSIZE;
    private final static int	HASH_BUCKETS_FIELD_TOP = 0;
    private final static int	HASH_BUCKETS_FIELD_LOCK = 1;
    private final static long	HASH_BUCKETS_TOP_FIELD_OFFSET = HASH_BUCKETS_BASE_OFFSET + HASH_BUCKETS_FIELD_TOP * WORDSIZE;
    private final static long	HASH_BUCKETS_LOCK_FIELD_OFFSET = HASH_BUCKETS_BASE_OFFSET + HASH_BUCKETS_FIELD_LOCK * WORDSIZE;
	
    /**
     * get the long address of the given base offset and field index
     * @param aBaseOffset base offset 
     * @param aFieldIndex field index
     * @return address of the given base offset and field index
     */
    private final static long	GetLongAddress( long aBaseOffset, long aFieldIndex )
    {
    	return aBaseOffset + aFieldIndex * WORDSIZE;
    }
    
	/**
	 * utility method to calculate the total size of the binary map including overhead based on 
	 */
	private final static long CalculateBinaryMapSize( long aNoOfBuckets, long aNoOfRecord, int aKeySize, int aValueSize )
	{
		long noOfBuckets = aNoOfBuckets;
		long recordSize = LINK_VALUE_SIZE + aKeySize + aValueSize;
		return MAP_HEADER_NO_OF_FIELDS * WORDSIZE + HASH_BUCKETS_NO_OF_FIELDS * WORDSIZE * noOfBuckets + recordSize * aNoOfRecord;
	}
	
	/**
	 * utility method to calculate the closest power of 2 of the given value
	 */
	final static long GetClosestPowerOfTwo( long aValue )
	{
		long value = aValue;
		value--;
		value |= value >> 1;
    	value |= value >> 2;
    	value |= value >> 4;
    	value |= value >> 8;
    	value |= value >> 16;
    	value++;
    	return value;
	}
	
	/**
	 * check if map persistent files exist
	 * @param aMemoryMappedFilenamePrefix file name prefix for the memory mapped file
	 * @return true if the file exist already
	 */
	public final static boolean DoesMapExist( String aMemoryMappedFilenamePrefix )
	{
		return MemoryMappedFileBackingStore.DoesMapExist(aMemoryMappedFilenamePrefix);
	}

	private final long maxMapSize;          // maximum size of map in bytes
    private final boolean concurrentMap; // indicates if the map is accessed by single thread or multiple threads
    private final KeyFunction keyFunction; // key function
    private final int recordSize; // size of each record - link + size of key + size of value
    private final int keySize;
    private final int valueSize;
    private final int recordValueOffset;
    private final MapBackingStore mapBackingStore;

    private long statisticsEvicts = 0;
    private long statisticsExpands = 0;		// statistics counters

    public HashBucketRegion hashBucketRegion;		// 2nd structure in mapped file
    public RecordRegion recordRegion;				// 3rd structure in mapped file
    
    private F1BinaryMapStatistics statistics = new F1BinaryMapStatistics();
    
	/**
	 * create F1BinaryMap based on existing memory mapped files
	 * @param aMemoryMappedFilenamePrefix prefix for the underlying memory mapped files
     * @param aKeySize size of the key in bytes
     * @param aValueSize size of the value in bytes
     * @param isConcurrentMap true indicates if this map is accessed by multiple threads and locking will be applied or false if
     * the map is accessed by a single thread only and no locking is required
     * @param aHashFunction hash function implementation
	 */
	public F1BinaryMap( final String aMemoryMappedFilenamePrefix, final long anInitialNoOfRecords, 
    		final int aKeySize, final int aValueSize,
    		final boolean isConcurrentMap, final KeyFunction aHashFunction )
    		throws IOException
    {
    	concurrentMap = isConcurrentMap;
    	keyFunction = aHashFunction;
    	keySize = aKeySize;
    	valueSize = aValueSize;
    	recordSize = LINK_VALUE_SIZE + keySize + valueSize;
    	recordValueOffset = RECORD_KEY_OFFSET + keySize;
    	mapBackingStore = new MemoryMappedFileBackingStore( aMemoryMappedFilenamePrefix, MAP_HEADER_FIELD_ADDRESS_MAX_MAP_SIZE );
		maxMapSize = mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_MAX_MAP_SIZE ); // extract manually
		assert( mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_RECORD_SIZE ) == recordSize ); // consistency check
    	hashBucketRegion = new HashBucketRegion( mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS ) );		// can now set number of hash buckets
    	recordRegion = new RecordRegion();
    }
	
	/**
	 * create F1BinaryMap
	 * @param aMemoryMappedFilenamePrefix prefix for the underlying memory mapped files
     * @param anInitialNoOfRecords initial no of recordRegion
     * @param aKeySize size of the key in bytes
     * @param aValueSize size of the value in bytes
     * @param aNoOfHashBuckets no of hash buckets to be used in the backing store
	 * @param aMaxNoOfRecords max no of records this map can hold
     * @param isConcurrentMap true indicates if this map is accessed by multiple threads and locking will be applied or false if
     * the map is accessed by a single thread only and no locking is required
     * @param aHashFunction hash function implementation
	 */
	public F1BinaryMap( final String aMemoryMappedFilenamePrefix, final long anInitialNoOfRecords, 
    		final int aKeySize, final int aValueSize, final long aSuggestedNoOfBuckets, final long aMaxNoOfRecords,
    		final boolean isConcurrentMap, final KeyFunction aHashFunction )
    		throws IOException
	{
    	concurrentMap = isConcurrentMap;
    	keyFunction = aHashFunction;
    	keySize = aKeySize;
    	valueSize = aValueSize;
    	recordSize = LINK_VALUE_SIZE + keySize + valueSize;
    	recordValueOffset = RECORD_KEY_OFFSET + keySize;
		long noOfBuckets = GetClosestPowerOfTwo( aSuggestedNoOfBuckets );
        maxMapSize = CalculateBinaryMapSize( noOfBuckets, aMaxNoOfRecords, keySize, valueSize );
    	long mapSize = MAP_HEADER_NO_OF_FIELDS * WORDSIZE + HASH_BUCKETS_NO_OF_FIELDS * WORDSIZE * noOfBuckets + recordSize * anInitialNoOfRecords;
    	mapBackingStore = new MemoryMappedFileBackingStore( aMemoryMappedFilenamePrefix, maxMapSize, anInitialNoOfRecords, 
    	    						aKeySize, aValueSize, noOfBuckets, aMaxNoOfRecords );

    	long noOfMemoryPages = mapBackingStore.calculateNoOfMemoryPages(mapSize); // rounding trick
    	initializeMapHeaderInfo( anInitialNoOfRecords, recordSize, noOfMemoryPages, noOfBuckets, maxMapSize ); // construction
    	hashBucketRegion = new HashBucketRegion( mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS ) );		// can now set number of hash buckets
    	recordRegion = new RecordRegion();			// can now use number of hash buckets
		hashBucketRegion.initializeHashBuckets();				// construction
	}
	
	/**
	 * create F1BinaryMap using direct memory
     * @param anInitialNoOfRecords initial no of recordRegion
     * @param aKeySize size of the key in bytes
     * @param aValueSize size of the value in bytes
     * @param aNoOfHashBuckets no of hash buckets to be used in the backing store
	 * @param aMaxNoOfRecords max no of records this map can hold
     * @param isConcurrentMap true indicates if this map is accessed by multiple threads and locking will be applied or false if
     * the map is accessed by a single thread only and no locking is required
     * @param aHashFunction hash function implementation
	 */
	public F1BinaryMap( final long anInitialNoOfRecords, 
    		final int aKeySize, final int aValueSize, final long aSuggestedNoOfBuckets, final long aMaxNoOfRecords,
    		final boolean isConcurrentMap, final KeyFunction aHashFunction )
    		throws IOException
	{
    	concurrentMap = isConcurrentMap;
    	keyFunction = aHashFunction;
    	keySize = aKeySize;
    	valueSize = aValueSize;
    	recordSize = LINK_VALUE_SIZE + keySize + valueSize;
    	recordValueOffset = RECORD_KEY_OFFSET + keySize;
    	// ensure no of buckets is in power of 2 to avoid using mod 
		long noOfBuckets = GetClosestPowerOfTwo( aSuggestedNoOfBuckets );
        maxMapSize = CalculateBinaryMapSize( noOfBuckets, aMaxNoOfRecords, keySize, valueSize );
    	long mapSize = MAP_HEADER_NO_OF_FIELDS * WORDSIZE + HASH_BUCKETS_NO_OF_FIELDS * WORDSIZE * noOfBuckets + recordSize * anInitialNoOfRecords;
    	mapBackingStore = new DirectMemoryBackingStore( maxMapSize );
    	long noOfMemoryPages = mapBackingStore.calculateNoOfMemoryPages(mapSize); // rounding trick
    	initializeMapHeaderInfo( anInitialNoOfRecords, recordSize, noOfMemoryPages, noOfBuckets, maxMapSize ); // construction
    	hashBucketRegion = new HashBucketRegion( mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS ) );		// can now set number of hash buckets
    	recordRegion = new RecordRegion();			// can now use number of hash buckets
		hashBucketRegion.initializeHashBuckets();				// construction
	}
	
	/**
	 * put key value into the map
	 * @param aKey key for lookup reference to the value
	 * @param aValue value to be placed into the map
	 */
	public final void put( final LongDirectBuffer aKey, final LongDirectBuffer aValue )
	{
		putRecord( aKey, 0, aValue, 0 );
	}
	
	/**
	 * put key value into the map
	 * @param aKey key for lookup reference to the value
	 * @param aKeyStartIndex byte start index of the key in the key buffer
	 * @param aValue value to be placed into the map
	 * @param aValueStartIndex start index of the value from the buffer
	 */
	public final void put( final LongDirectBuffer aKey, final long aKeyStartIndex,
			final LongDirectBuffer aValue, final long aValueStartIndex )
	{
		putRecord( aKey, aKeyStartIndex, aValue, aValueStartIndex );
	}
	
	/**
	 * get the value corresponding to the given key
	 * @param aKey key for lookup reference to the value
	 * @param aValue value buffer to be reused to copy the value into
	 * @return true if the value exists and has been copied to the aValue buffer 
	 * or false if the value does not exist and nothing has been performed on the aValue buffer
	 */
	public final boolean get( final LongDirectBuffer aKey, final LongMutableDirectBuffer aValue )
	{
		return getRecord( aKey, 0, aValue, 0 ) != NULL;
	}
	
	/**
	 * get the value corresponding to the given key
	 * @param aKey key for lookup reference to the value
	 * @param aKeyStartIndex byte start index of the key in the key buffer
	 * @param aValue value buffer to be reused to copy the value into
	 * @param aValueStartIndex byte start index of the value in the value buffer
	 * @return true if the value exists and has been copied to the aValue buffer 
	 * or false if the value does not exist and nothing has been performed on the aValue buffer
	 */
	public final boolean get( final LongDirectBuffer aKey, final long aKeyStartIndex,
						final LongMutableDirectBuffer aValue, final long aValueStartIndex )
	{
		return getRecord( aKey, aKeyStartIndex, aValue, aValueStartIndex ) != NULL;
	}
	
	/**
	 * get the value corresponding to the given key without copying the data. This method will throw RuntimeException if
	 * F1BinaryMap is set to be a concurrent map. Extreme caution has to be in place when using this method since this method will position the given buffer
	 * to the location of the value of the given key but if the record is changed after this method is called then the content in the given buffer will be undefined
	 * @param aKey key for lookup reference to the value
	 * @param aValue value buffer to be reused to copy the value into
	 * @return true if the value exists and has been copied to the aValue buffer 
	 * or false if the value does not exist and nothing has been performed on the aValue buffer
	 */
	public final boolean getWithZeroCopy( final LongDirectBuffer aKey, final LongMutableDirectBuffer aValue )
	{
		return getRecordZeroCopy( aKey, 0, aValue, 0 ) != NULL;
	}
	
	/**
	 * get the value corresponding to the given key without copying the data. This method will throw RuntimeException if
	 * F1BinaryMap is set to be a concurrent map. Extreme caution has to be in place when using this method since this method will position the given buffer
	 * to the location of the value of the given key but if the record is changed after this method is called then the content in the given buffer will be undefined
	 * @param aKey key for lookup reference to the value
	 * @param aKeyStartIndex byte start index of the key in the key buffer
	 * @param aValue value buffer to be reused to copy the value into
	 * @param aValueStartIndex byte start index of the value in the value buffer
	 * @return true if the value exists and has been copied to the aValue buffer 
	 * or false if the value does not exist and nothing has been performed on the aValue buffer
	 */
	public final boolean getWithZeroCopy( final LongDirectBuffer aKey, final long aKeyStartIndex,
						final LongMutableDirectBuffer aValue, final long aValueStartIndex )
	{
		return getRecordZeroCopy( aKey, aKeyStartIndex, aValue, aValueStartIndex ) != NULL;
	}
	
	/**
	 * remove the value corresponding to the given key
	 * @param aKey key for lookup reference to the value
	 * @return true if the value exists and has been removed or false if no such value exists
	 */
	public final boolean remove( final LongDirectBuffer aKey )
	{
		return remove( aKey, 0 );
	}
	
	/**
	 * check if the value exists in the map corresponding to the given key
	 * @param aKey key for lookup reference to the value
	 * @return true if the value exists or false if no such value exists
	 */
	public final boolean contains( final LongDirectBuffer aKey )
	{
		return getRecordPosition( aKey, 0 ) != NULL;
	}
	
	/**
	 * check if the value exists in the map corresponding to the given key
	 * @param aKey key for lookup reference to the value
	 * @param aKeyStartIndex byte start index of the key in the key buffer
	 * @return true if the value exists or false if no such value exists
	 */
	public final boolean contains( final LongDirectBuffer aKey, final long aKeyStartIndex )
	{
		return getRecordPosition( aKey, aKeyStartIndex ) != NULL;
	}
	
	/**
	 * get maximum map size in bytes
	 * @return maximum map size in bytes
	 */
	public long getMaxMapSize()
	{
		return maxMapSize;
	}
	
    /**
     * initialize map header information
     * @param aNoOfRecords no of recordRegion
     * @param aRecordSize size of each record
     * @param aNoOfMemoryPages no of memory pages
     * @param aNoOfBuckets no of buckets 
     * @param aMaxMapSize maximum map size
     */
	private void initializeMapHeaderInfo( final long aNoOfRecords, final long aRecordSize, final long aNoOfMemoryPages, final long aNoOfBuckets, final long aMaxMapSize ) 
	{
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_LOCK, VALUE_UNLOCKED );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SBRK, aNoOfRecords );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END, 0 );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE, NULL );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_RECORD_SIZE, aRecordSize );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_MEMORY_PAGES, aNoOfMemoryPages );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS, aNoOfBuckets );
		mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_MAX_MAP_SIZE, aMaxMapSize );
	}
	
	/**
	 * returns the number of records contained within the map
	 * @return number of records contained within the map
	 */
	public final long getSize()
	{ 
		if ( concurrentMap )
		{
			return mapBackingStore.getLongVolatileFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE );
		}
		else
		{
			return mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE );
		}
	}
	
	/**
	 * increment size of the map
	 */
	private void incrementSize()
	{
		if ( concurrentMap )
		{
	    	long oldSize = getSize();
	    	while( !mapBackingStore.compareAndPutLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE, oldSize, oldSize+1 ) )
	    	{
	    		oldSize = getSize();
	    	}			
		}
		else
		{
			long size = getSize() + 1;
			mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE, size );
		}
	}
	
	/**
	 * decrement size of the map
	 */
	private void decrementSize()
	{
		if ( concurrentMap )
		{
	    	long oldSize = getSize();
	    	while( !mapBackingStore.compareAndPutLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE, oldSize, oldSize-1 ) )
	    	{
	    		oldSize = getSize();
	    	}			
		}
		else
		{
			long size = getSize() - 1;
			mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE, size );
		}
	}

    /**
     * HashBucketRegion is the second structure within the Map Backing Store to host the information
     * for the hash buckets
     */
    private class HashBucketRegion
    {
    	private final int noOfBuckets;
    	private final int modFactor;
    	
    	/**
    	 * create hash buckets
    	 * @param aNoOfBuckets no of buckets 
    	 */
    	private HashBucketRegion( final long aNoOfBuckets )
    	{
    		noOfBuckets = (int)aNoOfBuckets;
    		modFactor = noOfBuckets - 1;
    	}
    	
    	/**
    	 * get top field address
    	 * @param aBucketIndex bucket index
    	 * @return top field address for the given bucket
    	 */
    	private long getTopFieldAddress( int aBucketIndex )
    	{
    		return HASH_BUCKETS_TOP_FIELD_OFFSET + aBucketIndex * HASH_BUCKETS_STRIDE;
    	}
    	
    	/**
    	 * get lock field address
    	 * @param aBucketIndex bucket index
    	 * @return lock field address for the given bucket
    	 */
    	private long getLockFieldAddress( int aBucketIndex )
    	{
    		return HASH_BUCKETS_LOCK_FIELD_OFFSET + aBucketIndex * HASH_BUCKETS_STRIDE;
    	}
    	
    	/**
    	 * get top value of the given bucket
    	 * @param aBucketIndex index of the bucket
    	 * @return top value of the given bucket
    	 */
    	private long getTop( final int aBucketIndex )
    	{
    		return mapBackingStore.getLongFromMemoryRegion( getTopFieldAddress(aBucketIndex) );
    	}
    	
    	/**
    	 * update top value of the given bucket
    	 * @param aBucketIndex index of the bucket
    	 * @param aValue top value of the given bucket
    	 */
    	private void updateTop( final int aBucketIndex, final long aValue )
    	{
    		mapBackingStore.putLongInMemoryRegion( getTopFieldAddress(aBucketIndex), aValue );
    	}
    	
    	/**
    	 * lock the given bucket
    	 * @param aBucketIndex index of the bucket
    	 */
    	private void lock( final int aBucketIndex )
    	{
    		if ( concurrentMap )
    		{
    			mapBackingStore.putLongInMemoryRegion( getLockFieldAddress(aBucketIndex), VALUE_LOCKED, VALUE_UNLOCKED );
    		}
    	}
    	
       	/**
    	 * unlock the given bucket
    	 * @param aBucketIndex index of the bucket
    	 */
    	private void unlock( final int aBucketIndex )
    	{
    		if ( concurrentMap )
    		{
    			mapBackingStore.putLongInMemoryRegion( getLockFieldAddress(aBucketIndex), VALUE_UNLOCKED );
    		}
    	}
    	
    	/**
    	 * initialize hash buckets values
    	 */
    	private void initializeHashBuckets()
    	{
    		for( int bucketIndex=0; bucketIndex<noOfBuckets; bucketIndex++ )
    		{
    			mapBackingStore.putLongInMemoryRegion( getTopFieldAddress(bucketIndex), NULL );
    			mapBackingStore.putLongInMemoryRegion( getLockFieldAddress(bucketIndex), VALUE_UNLOCKED );
    		}
    	}
    	
    	/**
    	 * get the positive hash code from the given key
    	 * @param aKey key 
    	 * @param aKeyStartIndex starting index of the key
    	 * @return hash value
    	 */
    	private int hash( final LongDirectBuffer aKey, final long aStartIndex ) 
    	{ 
    		return Math.abs( (int)keyFunction.hash(aKey, aStartIndex, keySize) & modFactor ); 
    	} // convert signed int to unsigned long
    } // HashBucketRegion

    /**
     * RecordRegion manages memory region for storing the actual data record. Each data record consists of the 
     * 1) flag to indicates if the record position has record
     * 2) link to the next record(long), 
     * 3) the key , 
     * 4) and the value
     */
    private class RecordRegion
    {
    	private final long baseOffset;
    	
    	/**
    	 * create record region
    	 */
    	private RecordRegion()
    	{
    		baseOffset = HASH_BUCKETS_BASE_OFFSET + HASH_BUCKETS_NO_OF_FIELDS * WORDSIZE * hashBucketRegion.noOfBuckets;
    	}
    	
    	/**
    	 * get record's memory address
    	 * @param aRecordPosition record number
    	 * @return record's memory address
    	 */
    	private long getRecordMemoryAddress( final long aRecordPosition )
    	{
    		return baseOffset + aRecordPosition * recordSize;
    	}
    	
        /**
         * get link value from the record of the given record number
         * @param aRecordPosition record number
         * @return link value of the given record number
         */
    	private long getLinkValue( final long aRecordPosition )
    	{
    		return mapBackingStore.getLongFromMemoryRegion( getRecordMemoryAddress( aRecordPosition ) );
    	}
    	
    	/**
    	 * set link value to the record of the given record number
    	 * @param aRecordPosition record number
    	 * @param aValue new value for the link
    	 */
    	private void updateLinkValue( final long aRecordPosition, final long aValue )
    	{
    		mapBackingStore.putLongInMemoryRegion( getRecordMemoryAddress( aRecordPosition ), aValue );
    	}
    	
    	/**
    	 * copy the key from given buffer into the record region
    	 * @param aRecordPosition record number of the target record to be copied in
    	 * @param aKey key buffer the record will be copied into 
    	 * @param aKeyStartIndex key buffer starting index
    	 */
    	private void copyKeyToRecordRegion( final long aRecordPosition, final LongDirectBuffer aKey, final long aKeyStartIndex )
    	{
    		mapBackingStore.putBytesToMemoryRegion( getRecordMemoryAddress( aRecordPosition ) + RECORD_KEY_OFFSET, keySize, aKey, aKeyStartIndex );
    	}
    	
    	/**
    	 * copy the value identified by the record number from the record region into the given buffer
    	 * @param aRecordPosition record number of the target record to be copied in
    	 * @param aValue value buffer the record will be copied into 
    	 * @param aValueStartIndex key buffer starting index
    	 */
    	private void copyValueFromRecordRegion( final long aRecordPosition, final LongMutableDirectBuffer aValue, final long aValueStartIndex )
    	{
    		mapBackingStore.getBytesFromMemoryRegion( getRecordMemoryAddress( aRecordPosition ) + recordValueOffset, valueSize, aValue, aValueStartIndex );
    	}
    	
    	/**
    	 * set the memory address identified by the record number from the record region into the given buffer
    	 * @param aRecordPosition record number of the target record to be copied in
    	 * @param aValue value buffer the record will be copied into 
    	 * @param aValueStartIndex key buffer starting index
    	 */
    	private void setMemoryAddressFromRecordRegion( final long aRecordPosition, final LongMutableDirectBuffer aValue, final long aValueStartIndex )
    	{
    		long recordMemoryAddress = getRecordMemoryAddress( aRecordPosition ) + recordValueOffset;
    		LongAtomicBuffer memoryRegion =  mapBackingStore.getMemoryRegion( recordMemoryAddress );
    		long memoryBufferAddress =  mapBackingStore.getMemoryBufferAddress( recordMemoryAddress );
    		aValue.wrap( memoryRegion.addressOffset() + memoryBufferAddress, valueSize );
    	}
    	
    	/**
    	 * copy the value from given buffer into the record region
    	 * @param aRecordPosition record number of the target record to be copied in
    	 * @param aValue value buffer the record will be copied into 
    	 * @param aValueStartIndex key buffer starting index
    	 */
    	private void copyValueToRecordRegion( final long aRecordPosition, final LongDirectBuffer aValue, final long aValueStartIndex )
    	{
    		mapBackingStore.putBytesToMemoryRegion( getRecordMemoryAddress( aRecordPosition ) + recordValueOffset, valueSize, aValue, aValueStartIndex );
    	}
    	
    	/**
    	 * allocate new data record identifier by the key in the given bucket
    	 * @param aKey key of the new data record
    	 * @param aKeyStartIndex starting index where the key is located in the key buffer
    	 * @param aBucketIndex index of the bucket
    	 * @return record number created
    	 */
    	private long newRecord( final LongDirectBuffer aKey, final long aKeyStartIndex, final int aBucketIndex )
    	{
    		// TODO potentially optimize it without the need to resolve the address everytime 
		    long recordPosition = allocateNewRecordInBucket( aBucketIndex );
		    updateLinkValue( recordPosition, NULL );
		    copyKeyToRecordRegion( recordPosition, aKey, aKeyStartIndex );
		    return recordPosition;
    	}
    	
    	/**
    	 * delete data record identified by the record number
    	 * @param aRecordPosition record number of the record to be deleted
    	 */
    	private void deleteRecord( final long aRecordPosition )
    	{
		    free( aRecordPosition );    		
    	}
    	
    	/**
    	 * convert key of the given record position to human readable string
    	 * @param aRecordPosition record position
    	 * @param aKeyValueToString string converter for the key
    	 * @return human readable string version of the key
    	 */
    	private String convertKey( final long aRecordPosition, KeyValueToString aKeyValueToString )
    	{
    		long address = getRecordMemoryAddress( aRecordPosition ) + RECORD_KEY_OFFSET;
    		long memoryBufferAddress =  mapBackingStore.getMemoryBufferAddress( address );
    		LongDirectBuffer buffer =  mapBackingStore.getMemoryRegion( address );
    		return aKeyValueToString.convertKey( buffer, memoryBufferAddress, keySize );
    	}
    	
    	/**
    	 * convert value of the given record position to human readable string
    	 * @param aRecordPosition record position
    	 * @param aKeyValueToString string converter for the key
    	 * @return human readable string version of the value of the record
    	 */
    	private String convertValue( final long aRecordPosition, KeyValueToString aKeyValueToString )
    	{
    		long address = getRecordMemoryAddress( aRecordPosition ) + recordValueOffset;
    		long memoryBufferAddress =  mapBackingStore.getMemoryBufferAddress( address );
    		LongDirectBuffer buffer =  mapBackingStore.getMemoryRegion( address );
    		return aKeyValueToString.convertValue( buffer, memoryBufferAddress, valueSize );
    	}
    }
    
	/**
	 * lock the entire map
	 */
	private void lockMap()
	{
		if ( concurrentMap )
		{
			while( !mapBackingStore.compareAndPutLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_LOCK, VALUE_LOCKED, VALUE_UNLOCKED ) )
	    	{
	    	}
		}
	}
	
   	/**
	 * unlock the entire map
	 */
	private void unlockMap()
	{
		if ( concurrentMap )
		{
			mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_LOCK, VALUE_UNLOCKED );
		}
	}
    
    /**
     * free the  record identified by the given record number
     * @param aRecordPosition record number of the record to be free
     */
	private void free( final long aRecordPosition ) 
	{
		lockMap();
	    try 
	    {
	    	recordRegion.updateLinkValue( aRecordPosition, mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE )); // chain to head of freelist
	    	mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE, aRecordPosition );			// set freelist head to freed node
	    } 
	    finally 
	    {
	    	unlockMap();
	    } // try
	} // free
	
    /**
     * check if the given key is equivalent to the given record number
     * @param aKey key 
     * @param aKeyStartIndex start index of the key in the key buffer
     * @param aRecordPosition record number
     * @return true if the key is equivalent to the given record number or false if it is not
     */
	private boolean equals( final LongDirectBuffer aKey, final long aKeyStartIndex, final long aRecordPosition ) 
	{
		// TODO optimize it
		long keyAddress = recordRegion.getRecordMemoryAddress( aRecordPosition ) + RECORD_KEY_OFFSET;
		LongAtomicBuffer keyBuffer = mapBackingStore.getMemoryRegion( keyAddress );
		long keyStartIndex =  mapBackingStore.getMemoryBufferAddress( keyAddress );
		
		return keyFunction.equals(aKey, aKeyStartIndex, keyBuffer, keyStartIndex, keySize);
	} // equals

	/**
	 * allocate new record in the given bucket
	 * @param aBucketIndex index of the bucket the new record will be created
	 * @return newly created record number
	 */
	private long allocateNewRecordInBucket( final int aBucketIndex ) 
	{
	    lockMap();
	    try 
	    {
	    	long availableRecordIndex = mapBackingStore.getLongFromMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE );
			// free nodes ?
			if ( availableRecordIndex != NULL ) 
			{			
				mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE, recordRegion.getLinkValue( availableRecordIndex ) );
			} 
			else 
			{					
				// allocate new new node
			    if ( mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END ) >= mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SBRK ) ) 
			    {	
			    	// cannot expand => evict
			    	long expandedMapBackingStoreSize = mapBackingStore.expand( MAP_HEADER_FIELD_ADDRESS_NO_OF_MEMORY_PAGES );
			    	if ( expandedMapBackingStoreSize == -1 ) 
			    	{	
			    		long recordPosition;
			    		for ( int currentBucketIndex = (aBucketIndex + 1) & hashBucketRegion.modFactor; ; currentBucketIndex = (currentBucketIndex + 1) & hashBucketRegion.modFactor ) 
			    		{
			    			// cycled ?
			    			if ( currentBucketIndex == aBucketIndex ) 
			    			{	
			    				unlockMap(); // consistent state
			    				throw new RuntimeException( "Fatal: Inconsistent state" );
			    			} // if
			    			hashBucketRegion.lock( currentBucketIndex );
			    			try 
			    			{
			    				recordPosition = hashBucketRegion.getTop( currentBucketIndex );
			    				// non-empty hash chain
			    				if ( recordPosition != NULL ) 
			    				{	
			    					// remove first (head) node
			    					if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
			    					{ 
			    						hashBucketRegion.updateTop( currentBucketIndex, NULL ); // remove only node
			    					} 
			    					else 
			    					{
			    						long previousRecord = recordPosition; // need previous node
			    						// search hash chain
			    						for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
			    						{ 
			    							if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
			    							{
			    								break; 
			    							}
			    							previousRecord = recordPosition;
			    						} // for
			    						recordRegion.updateLinkValue( previousRecord, NULL ); // remove last node
			    					} // if
			    					// record removed
			    					break;
			    				} // if
			    			} 
			    			finally 
			    			{
			    				hashBucketRegion.unlock( currentBucketIndex );
			    			} // try
			    		} // for
			    		statisticsEvicts += 1;
			    		return recordPosition;
			    	} // if
	
			    	// expand
			    	statisticsExpands += 1;
			    	long maxNoOfRecords = (expandedMapBackingStoreSize - recordRegion.baseOffset) / recordSize;
				    mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SBRK, maxNoOfRecords );
			    } // if
			    availableRecordIndex = mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END );			// take next free record
			    mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END, availableRecordIndex + 1 );
			} // if
			return availableRecordIndex;
	    } 
	    finally 
	    {
	    	unlockMap();
	    } // try
	} // allocateNewRecordInBucket
	
	/**
	 * get record position based on the given key
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @return record position
	 */
	public long getRecordPosition( final LongDirectBuffer aKey, final long aKeyStartIndex ) 
	{			
	    int bucket = hashBucketRegion.hash( aKey, aKeyStartIndex );
	    hashBucketRegion.lock( bucket );
	    try 
	    {
	    	long recordPosition = hashBucketRegion.getTop( bucket );
	    	if ( recordPosition != NULL ) 
	    	{			// non-empty hash chain
	    		for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{ // search hash chain
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{
	    				break; // found ?
	    			}
	    			if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
	    			{ 
	    				recordPosition = NULL; 
	    				break; 
	    			} // not found ?
	    		} // for
	    	} // if
	    	return recordPosition;
	    } 
	    finally 
	    {
	    	hashBucketRegion.unlock( bucket );
	    } // try
	} // get

	/**
	 * get the record of the given key and copy into the given buffer
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @param aValue value buffer
	 * @param aValueStartIndex start index of the value should be copied to
	 * @return record position
	 */
	public long getRecord( final LongDirectBuffer aKey, final long aKeyStartIndex, final LongMutableDirectBuffer aValue, final long aValueStartIndex ) 
	{		
	    int bucket = hashBucketRegion.hash( aKey, aKeyStartIndex );
	    hashBucketRegion.lock( bucket );
	    try 
	    {
	    	long recordPosition = hashBucketRegion.getTop( bucket );
	    	if ( recordPosition != NULL ) 
	    	{			
	    		// non-empty hash chain
	    		for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{ // search hash chain
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{	// found ?
	    				recordRegion.copyValueFromRecordRegion( recordPosition, aValue, aValueStartIndex);
	    				break;
	    			} // if
	    			if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
	    			{ 
	    				recordPosition = NULL; 
	    				break; 
	    			} // not found
	    		} // for
	    	} // if
	    	return recordPosition;
	    } 
	    finally 
	    {
	    	hashBucketRegion.unlock( bucket );
	    } // try
	} // get
	
	/**
	 * get the record of the given key and map the address location to the given buffer without copying the data. This method will throw RuntimeException if
	 * F1BinaryMap is set to be a concurrent map. Extreme caution has to be in place when using this method since this method will position the given buffer
	 * to the location of the value of the given key but if the record is changed after this method is called then the content in the given buffer will be undefined
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @param aValue value buffer
	 * @param aValueStartIndex start index of the value should be copied to
	 * @return record position
	 */
	public long getRecordZeroCopy( final LongDirectBuffer aKey, final long aKeyStartIndex, final LongMutableDirectBuffer aValue, final long aValueStartIndex ) 
	{		
		if ( concurrentMap )
		{
			throw new RuntimeException( "This method cannot be used in concurrent mode" );
		}
	    int bucket = hashBucketRegion.hash( aKey, aKeyStartIndex );
	    hashBucketRegion.lock( bucket );
	    try 
	    {
	    	long recordPosition = hashBucketRegion.getTop( bucket );
	    	if ( recordPosition != NULL ) 
	    	{			
	    		// non-empty hash chain
	    		for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{ // search hash chain
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{	// found ?
	    				recordRegion.setMemoryAddressFromRecordRegion( recordPosition, aValue, aValueStartIndex);
	    				break;
	    			} // if
	    			if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
	    			{ 
	    				recordPosition = NULL; 
	    				break; 
	    			} // not found
	    		} // for
	    	} // if
	    	return recordPosition;
	    } 
	    finally 
	    {
	    	hashBucketRegion.unlock( bucket );
	    } // try
	} // get

	/**
	 * put the given record with the key into the map
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @param aValue value buffer
	 * @param aValueStartIndex start index of the value from the buffer
	 * @return record position
	 */
	final long putRecord( final LongDirectBuffer aKey, final long aKeyStartIndex, final LongDirectBuffer aValue, final long aValueStartIndex ) 
	{
	    int bucket = hashBucketRegion.hash( aKey, aKeyStartIndex );
	    hashBucketRegion.lock( bucket );
	    long recordPosition = NULL;
	    // check for update versus add
	    try 
	    {
	    	recordPosition = hashBucketRegion.getTop( bucket );
	    	if ( recordPosition != NULL ) 
	    	{			// non-empty hash chain
	    		for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{ 
	    			// search hash chain
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{	
	    				// found ?
	    				recordRegion.copyValueToRecordRegion( recordPosition, aValue, aValueStartIndex );
	    				return recordPosition;
	    			} // if
	    			if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
	    			{ 
	    				// not found
	    				break;
	    			} // if
	    		} // for
	    	} // if
	    } 
	    finally 
	    {
	    	hashBucketRegion.unlock( bucket );
	    } // try

	    // existing record not found. create a new record 
	    boolean found = false;

	    // speculatively get record to hold new data
	    long newRecordPosition = recordRegion.newRecord(aKey, aKeyStartIndex, bucket);	// create record and initialize

	    // Race to add new record with given key, but could lose and becomes update.
	    // Update => allocated record unused.
	    hashBucketRegion.lock( bucket );   
	    try 
	    {
	    	long top = hashBucketRegion.getTop( bucket );
	    	recordPosition = top;
	    	if ( recordPosition == NULL ) 
	    	{			
	    		// empty hash chain
	    		recordRegion.copyValueToRecordRegion( newRecordPosition, aValue, aValueStartIndex );
	    		hashBucketRegion.updateTop( bucket, newRecordPosition ); // set bucket to new node
	    		recordPosition = newRecordPosition;
	    	} 
	    	else 
	    	{
	    		for ( ;; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{    			
	    			// search hash chain
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{	
	    				// found ?
	    				recordRegion.copyValueToRecordRegion( recordPosition, aValue, aValueStartIndex );
	    				found = true;
	    				break;
	    			} // if
	    			if ( recordRegion.getLinkValue( recordPosition ) == NULL ) 
	    			{ 
	    				// not found
	    				recordRegion.copyValueToRecordRegion( newRecordPosition, aValue, aValueStartIndex );
	    				recordRegion.updateLinkValue( newRecordPosition, top );
	    				hashBucketRegion.updateTop( bucket, newRecordPosition);
	    				recordPosition = newRecordPosition;
	    				break;
	    			} // if
	    		} // for
	    	} // if
	    	return recordPosition;
	    } 
	    finally 
	    {
	    	if ( recordPosition != NULL )
	    	{
	    		incrementSize();
	    	}
	    	hashBucketRegion.unlock( bucket );
	    	if ( found ) 
	    	{
	    		free( newRecordPosition );	// return unused record
	    	} // if
	    } // try
	} // put

	/**
	 * remove the record of the given key
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @return true if the key existed and has been removed or false if the key does not exist
	 */
	final boolean remove( final LongDirectBuffer aKey, final long aKeyStartIndex ) 
	{
	    boolean found = false;
	    int bucket = hashBucketRegion.hash( aKey, aKeyStartIndex );
	    long recordPosition = 0;

	    hashBucketRegion.lock( bucket );
	    try 
	    {
	    	recordPosition = hashBucketRegion.getTop( bucket );
	    	if ( recordPosition == NULL ) 
	    	{
	    		return false;	// non-existing key
	    	}
	    	if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    	{		// found, remove first (head) node
	    		hashBucketRegion.updateTop( bucket, recordRegion.getLinkValue( recordPosition ) ); // set bucket to next node on freelist
	    	} 
	    	else 
	    	{
	    		long previousRecord = recordPosition;			// need previous node
	    		for ( recordPosition = recordRegion.getLinkValue( recordPosition );; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{ 
	    			// search hash chain
	    			if ( recordPosition == NULL ) 
	    			{
	    				return false; // non-existing key
	    			}
	    			if ( equals( aKey, aKeyStartIndex, recordPosition ) ) 
	    			{	// found ?
	    				long temp = recordRegion.getLinkValue( recordPosition ); // link around removed node
	    				recordRegion.updateLinkValue( previousRecord, temp ); // link around removed node
	    				break;
	    			} // if
	    			previousRecord = recordPosition;
	    		} // for
	    	} // if
	    	// record is now removed
	    	found = true;
	    	return true;
	    } 
	    finally 
	    {
	    	if ( found ) 
	    	{
	    		recordRegion.deleteRecord( recordPosition );		// return unused record
	    		decrementSize();
	    	} // if
	    	hashBucketRegion.unlock( bucket );
	    } // try
	} // remove

	/**
	 * dispose the map and releases all the resources
	 * @param shouldEraseAllPersistedMemory true will remove all the existing persisted memory from the system
	 */
	public void dispose( boolean shouldEraseAllPersistedMemory )
	{
		mapBackingStore.dispose(shouldEraseAllPersistedMemory);
	}
	
	/**
	 * iterate all the key and values by traversing the map. This method can be used by non-concurrent map
	 * and memory access will be provided in no copy style. Access this method with concurrent map will get run time exception
	 * @param anIterator iterator
	 */
	public void traverse( final F1BinaryMapIterator anIterator )
	{
		if ( concurrentMap )
		{
			throw new RuntimeException( "Cannot use this method to traverse concurrent map. Please use traverse( anIterator, aBuffer )" );
		}
		// iterate through
	    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
	    {
	    	long bucketTop = hashBucketRegion.getTop( bucketIndex );
	    	if ( bucketTop != NULL ) 
	    	{
	    		for ( long recordPosition = hashBucketRegion.getTop( bucketIndex  ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{
	        		long address = recordRegion.getRecordMemoryAddress( recordPosition );
	        		long memoryBufferAddress =  mapBackingStore.getMemoryBufferAddress( address );
	        		long keyStartIndex = memoryBufferAddress + RECORD_KEY_OFFSET;
	        		long valueStartIndex = memoryBufferAddress + recordValueOffset;
	        		LongDirectBuffer buffer =  mapBackingStore.getMemoryRegion( address );
	        		anIterator.iterate( buffer, keyStartIndex, keySize, valueStartIndex, valueSize );
	    		} // for
	    	} // if
	    } // for
	}
	
	/**
	 * iterate all long values of the given value offset by traversing the map. This method can be used by non-concurrent map
	 * and memory access will be provided in no copy style. Access this method with concurrent map will get run time exception
	 * @param anIterator iterator
	 * @param aValueOffset address offset from the value position where the long value is located in each record
	 */
	public void traverse( final F1BinaryMapLongValueIterator anIterator, int aValueOffset )
	{
		// TODO how not to repeat the logic
		if ( concurrentMap )
		{
			throw new RuntimeException( "Cannot use this method to traverse concurrent map. Please use traverse( anIterator, aBuffer )" );
		}
		// iterate through
	    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
	    {
	    	long bucketTop = hashBucketRegion.getTop( bucketIndex );
	    	if ( bucketTop != NULL ) 
	    	{
	    		for ( long recordPosition = hashBucketRegion.getTop( bucketIndex  ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
	    		{
	        		long address = recordRegion.getRecordMemoryAddress( recordPosition );
	        		long memoryBufferAddress =  mapBackingStore.getMemoryBufferAddress( address );
	        		long longValueStartIndex = memoryBufferAddress + recordValueOffset + aValueOffset;
	        		LongDirectBuffer buffer =  mapBackingStore.getMemoryRegion( address );
	        		anIterator.iterate( buffer.getLong( longValueStartIndex ) );
	    		} // for
	    	} // if
	    } // for		
	}
	
	/**
	 * clear all the values from the map
	 */
	public void clear()
	{
		try
		{
			// lock everything first
			lockMap();
		    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
		    {
		    	hashBucketRegion.lock(bucketIndex);
		    }
		    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
		    {
		    	long bucketTop = hashBucketRegion.getTop( bucketIndex );
		    	if ( bucketTop != NULL ) 
		    	{
		    		for ( long recordPosition = hashBucketRegion.getTop( bucketIndex  ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
		    		{
		    			recordRegion.updateLinkValue( recordPosition, NULL );
		    		} // for
		    		hashBucketRegion.updateTop( bucketIndex, NULL );
		    	} // if
		    } // for
		    mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END, 0 );
		    mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE, NULL );
		    mapBackingStore.putLongInHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SIZE, 0 );
		}
		finally
		{
			// unlock everything
		    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
		    {
		    	hashBucketRegion.unlock(bucketIndex);
		    }
			unlockMap();
		}
	} // dump
	
	/**
	 * dump the map backing store information to console
	 * @param aKeyValueToString converter to convert key and value to human readable form
	 */
	public void dump( KeyValueToString aKeyValueToString ) 
	{
		lockMap();
		try
		{
		    final int perLine = 10;			// nodes per line of output
		    System.out.println( "----------------------- Map Header Regions Dump -----------------------" );
		    System.out.println( "Lock: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_LOCK ) );
		    System.out.println( "Sbrk: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_SBRK ) );
		    System.out.println( "End: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_END ) );
		    System.out.println( "Free: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE ) );
		    System.out.println( "Record Size: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_RECORD_SIZE ) );
		    System.out.println( "No of Memory Pages: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_MEMORY_PAGES ) );
		    System.out.println( "No of Buckets: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_NO_OF_BUCKETS ) );
		    System.out.println( "Max Map Size: " + mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_MAX_MAP_SIZE ) );
		    
		    System.out.println( "----------------------- Start Hash Bucket Regions Dump -----------------------" );
		    System.out.println( "Buckets: ");
		    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
		    {
		    	long bucketTop = hashBucketRegion.getTop( bucketIndex );
		    	if ( bucketTop != NULL ) 
		    	{
		    		System.out.print( "    [" + bucketIndex + "] with top " + bucketTop + " = " );
		    		int count = 0;
		    		for ( long recordPosition = hashBucketRegion.getTop( bucketIndex  ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
		    		{
		    			System.out.print( "[key:" + recordRegion.convertKey(recordPosition, aKeyValueToString) + " record:" 
		    								+ recordRegion.convertValue(recordPosition, aKeyValueToString)  + "] -> ");
		    			if ( count == perLine ) 
		    			{
		    				System.out.print( "\n        " );
		    				count = 0;
		    			} // if
		    			count += 1;
		    		} // for
		    		System.out.println( "" );
		    	} // if
		    } // for
	
		    System.out.print( "FREELIST:\n    " );
		    int count = 0;
		    for ( long recordPosition = mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition) ) 
		    {
		    	System.out.print( "[" + recordPosition + "] -> " );
		    	if ( count == perLine ) 
		    	{
		    		System.out.print( "\n    " );
		    		count = 0;
		    	} // if
		    	count += 1;
		    } // for
		    System.out.println( "" );
		    System.out.println( "----------------------- End Hash Table Dump -----------------------" );
		}
		finally
		{
			unlockMap();
		}
	} // dump
	
	/**
	 * get statistics information
	 */
	public final F1BinaryMapStatistics statistics() 
	{
		lockMap();
		try
		{
		    long usedBuckets = 0, maxHashChain = 0, equalMax = 0, size = getSize();
	
		    for ( int bucketIndex = 0; bucketIndex < hashBucketRegion.noOfBuckets; bucketIndex += 1 ) 
		    {
				if ( hashBucketRegion.getTop( bucketIndex ) != NULL ) 
				{
				    usedBuckets += 1;
				    long count = 0;
				    for ( long recordPosition = hashBucketRegion.getTop( bucketIndex ); recordPosition != NULL; recordPosition = recordRegion.getLinkValue( recordPosition ) ) 
				    {
				    	count += 1;
				    } // for
				    if ( count > maxHashChain ) 
				    { 
				    	maxHashChain = count; 
				    	equalMax = 0; 
				    }
				    if ( count == maxHashChain ) 
				    {
				    	equalMax += 1;
				    }
				} // if
		    } // for
		    int freeList = 0;
		    for ( long index = mapBackingStore.getLongFromHeaderMemoryRegion( MAP_HEADER_FIELD_ADDRESS_FREE ); index != NULL; index = recordRegion.getLinkValue( index ) ) 
		    {
		    	freeList += 1;
		    } // for
		    statistics.setStatisticalInfo( usedBuckets, maxHashChain, equalMax, statisticsExpands, statisticsEvicts, freeList, size );
		    return statistics;
		}
		finally
		{
			unlockMap();
		}
	} // statistics
}
