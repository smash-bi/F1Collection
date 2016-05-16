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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import smash.f1.core.agrona.LongAtomicBuffer;
import smash.f1.core.agrona.LongDirectBuffer;
import smash.f1.core.agrona.LongMutableDirectBuffer;
import smash.f1.core.agrona.LongUnsafeBuffer;

/**
 * 
 * @author thomaslo
 *
 */
final class MemoryMappedFileBackingStore implements MapBackingStore
{
	private final static String MAP_HEADER_FILE_SUFFIX = ".0";
    private final static long MAX_FILE_BITS = 30;		// 2^N bytes in a file < 31
    private final static long MAX_FILE_SIZE = 1 << MAX_FILE_BITS; // maximum size of each subfile (power of 2)
    private final static long ONE_LEFT_SHIFT_MAX_FILE_BITS = 1 << MAX_FILE_BITS;
    
	/**
	 * check if map persistent files exist
	 * @param aMemoryMappedFilenamePrefix file name prefix for the memory mapped file
	 * @return true if the file exist already
	 */
	final static boolean DoesMapExist( String aMemoryMappedFilenamePrefix )
	{
		File memoryMappedFile = new File( aMemoryMappedFilenamePrefix + MAP_HEADER_FILE_SUFFIX );
		return memoryMappedFile.exists();
	}
  
    private final long maxFileSize;          // maximum size of a mapped file segment
    private final long maxBuffers;			// maximum file partitions
    private final String memoryMappedFilenamePrefix;	// file prefix for each mapped file
 
    private LongUnsafeBuffer headerMemoryRegion;
    private File headerMemoryMappedFileReference;
    private StraddleAtomicBuffer[] memoryRegions;			// memory-mapped buffers of file
    private File[] memoryMappedFileReferences;
    
    /**
     * create memory mapped file backing store based on existing memory mapped file
     * @param aMemoryMappedFilenamePrefix prefix for the underlying memory mapped files
     * @param aMaxMapSizeAddress address of the header that stores the max map size
     */
    MemoryMappedFileBackingStore( final String aMemoryMappedFilenamePrefix, final long aMaxMapSizeAddress ) throws IOException
    {
    	memoryMappedFilenamePrefix = aMemoryMappedFilenamePrefix;
    	boolean headerMemoryRegionCreated = initializeHeaderMemoryRegion(false);
    	if ( !headerMemoryRegionCreated )
    	{
    		throw new IOException( "Memory Mapped file does not exist" );
    	}
    	long maxMapSize = getLongFromHeaderMemoryRegion( aMaxMapSizeAddress );
    	maxFileSize = Math.min(maxMapSize, MAX_FILE_SIZE);
    	maxBuffers = calculateMaxNoOfBuffers(maxMapSize);		// number of files comprising hash table
    	initializeNonHeaderMemoryRegions();
    }
    
    /**
     * create memory mapped file backing store
     * @param aMaxMapSizeAddress address of the header that stores the max map size
     */
    MemoryMappedFileBackingStore( final String aMemoryMappedFilenamePrefix, final long aMaxMapSize, final long anInitialNoOfRecords, 
    		final int aKeySize, final int aValueSize, final long aNoOfBuckets, final long aMaxNoOfRecords ) throws IOException
	{
    	memoryMappedFilenamePrefix = aMemoryMappedFilenamePrefix; // copy
    	maxFileSize = Math.min(aMaxMapSize, MAX_FILE_SIZE);
    	maxBuffers = calculateMaxNoOfBuffers(aMaxMapSize);
    	boolean headerMemoryRegionCreated = initializeHeaderMemoryRegion(true);
    	if ( !headerMemoryRegionCreated )
    	{
    		throw new IOException( "Memory Mapped file exists already " + memoryMappedFilenamePrefix );
    	}
    	initializeNonHeaderMemoryRegions();
	}
    
    @Override
    public long calculateNoOfMemoryPages( final long aMapSize )
    {
    	return (aMapSize + MAX_FILE_SIZE - 1) / MAX_FILE_SIZE;
    }
    
    /**
     * calculate max no of buffers
     * @param aMaxMapSize maximum map size
     * @return calculated max no of buffers
     */
    private long calculateMaxNoOfBuffers( long aMaxMapSize )
    {
    	return (aMaxMapSize + MAX_FILE_SIZE - 1) / MAX_FILE_SIZE < 1 ? 1 : (aMaxMapSize + MAX_FILE_SIZE - 1) / MAX_FILE_SIZE;
    }
    
    /**
     * initialize header memory region
     * @param shouldCreate true if memory mapped file should be created
     * @return true if the header memory region has been created
     */
    private boolean initializeHeaderMemoryRegion( final boolean shouldCreate ) throws IOException
    {
    	headerMemoryMappedFileReference = new File( memoryMappedFilenamePrefix + MAP_HEADER_FILE_SUFFIX );	// query map header file
    	// before opening map header file, check if exists, otherwise created
    	boolean doesMapHeaderFileExist = headerMemoryMappedFileReference.exists() && headerMemoryMappedFileReference.isFile(); // file exists ?
    	if ( (shouldCreate && doesMapHeaderFileExist) || (!shouldCreate && !doesMapHeaderFileExist) )
    	{
    		return false;
    	}
    	LongUnsafeBuffer memoryMappedFileBuffer = createNewMemoryMappedFileUnsafeBuffer( headerMemoryMappedFileReference, 0 );
		headerMemoryRegion = memoryMappedFileBuffer;
		return true;
    }
    
    /**
     * initialize the rest of the memory regions
     */
    private void initializeNonHeaderMemoryRegions() throws IOException
    {
    	memoryRegions = new StraddleAtomicBuffer[(int)maxBuffers]; // memory-mapping buffers for each file in hash table
    	memoryRegions[0] = new StraddleAtomicBuffer( headerMemoryRegion );				// use existing open buffer
    	memoryMappedFileReferences = new File[(int)maxBuffers];
    	memoryMappedFileReferences[0] = headerMemoryMappedFileReference;
    	
    	File memoryMappedFile = null;
		for ( int memoryMappedFileIndex = 1; memoryMappedFileIndex < maxBuffers; memoryMappedFileIndex += 1 ) 
		{		// create remaining files
		    memoryMappedFile = new File( memoryMappedFilenamePrefix + "." + memoryMappedFileIndex );	// record file
		    memoryRegions[memoryMappedFileIndex] = new StraddleAtomicBuffer( createNewMemoryMappedFileUnsafeBuffer( memoryMappedFile, memoryMappedFileIndex ) );
		    if( memoryMappedFileIndex > 0 )
		    {
		    	memoryRegions[memoryMappedFileIndex-1].setSecondBuffer(memoryRegions[memoryMappedFileIndex].getFirstBuffer());
		    }
		} // for
    }
    
    /**
     * utility method to create a new memory mapped file in unsafe buffer
     * @param aFile file reference of the memory mapped file to be created
     * @param anIndex buffer index of the memory mapped file
     * @throws IOException 
     */
    private LongUnsafeBuffer createNewMemoryMappedFileUnsafeBuffer( final File aFile, final int anIndex ) throws IOException
    {
    	RandomAccessFile memoryMappedRandomAccessFile = new RandomAccessFile( aFile, "rw" );
	    memoryMappedRandomAccessFile.setLength( maxFileSize );
	    MappedByteBuffer memoryMappedFileByteBuffer = memoryMappedRandomAccessFile.getChannel().map( FileChannel.MapMode.READ_WRITE, 0, maxFileSize );
	    memoryMappedFileByteBuffer.load();			// load storage into physical memory
	    memoryMappedRandomAccessFile.close();
	    if ( memoryMappedFileReferences != null )
	    {
	    	memoryMappedFileReferences[anIndex] = aFile;
	    }
	    return new LongUnsafeBuffer( memoryMappedFileByteBuffer );
    }
    
	@Override
    public long getLongFromHeaderMemoryRegion( final long anAddress ) 
    {
    	return headerMemoryRegion.getLong( anAddress ); 
    }
	
	@Override
    public long getLongVolatileFromHeaderMemoryRegion( final long anAddress )
    {
		return headerMemoryRegion.getLongVolatile( anAddress );
    }
    
	@Override
    public void putLongInHeaderMemoryRegion( final long anAddress, final long aNewValue ) 
    {
    	headerMemoryRegion.putLong( anAddress , aNewValue ); 
    }
	
	@Override
    public boolean compareAndPutLongInHeaderMemoryRegion( final long anAddress, final long aNewValue, final long anOldValue ) 
    {
    	return headerMemoryRegion.compareAndSetLong( anAddress, anOldValue, aNewValue );
    }
	
	@Override
    public LongAtomicBuffer getMemoryRegion( final long anAddress )
    {
    	return memoryRegions[(int)(anAddress >>> MAX_FILE_BITS)];
    }
    
    @Override
    public long getMemoryBufferAddress( final long anAddress )
    {
    	return (anAddress & ONE_LEFT_SHIFT_MAX_FILE_BITS - 1);
    }
    
    @Override
    public long getLongFromMemoryRegion( final long anAddress ) 
    {
    	return getMemoryRegion( anAddress ).getLong( getMemoryBufferAddress( anAddress ) ); 
    }
    
    @Override
    public void putLongInMemoryRegion( final long anAddress, final long aNewValue ) 
    {
    	getMemoryRegion( anAddress ).putLong( getMemoryBufferAddress( anAddress ) , aNewValue ); 
    }
    
    @Override
    public void putLongInMemoryRegion( final long anAddress, final long aNewValue, final long anOldValue )
    {
    	long memoryBufferAddress = getMemoryBufferAddress( anAddress );
    	LongAtomicBuffer memoryRegion = getMemoryRegion( anAddress );
    	while( !memoryRegion.compareAndSetLong( memoryBufferAddress, anOldValue, aNewValue ) )
    	{
    	}    	
    }
    
    @Override
    public void getBytesFromMemoryRegion( final long anAddress, final int aLength, final LongMutableDirectBuffer aReceivingBuffer, final long aReceivingBufferStartIndex )
    {
    	aReceivingBuffer.putBytes( aReceivingBufferStartIndex, getMemoryRegion( anAddress ), getMemoryBufferAddress( anAddress ), aLength );
    }
    
    @Override
    public void putBytesToMemoryRegion( final long anAddress, final int aLength, final LongDirectBuffer aSourceBuffer, final long aSourceBufferStartIndex )
    {
    	aSourceBuffer.getBytes( aSourceBufferStartIndex, getMemoryRegion( anAddress ), getMemoryBufferAddress( anAddress ), aLength );
    }
    
    @Override
    public long expand( long aMapHeaderFieldAddressNoOfMemoryPages )
    {
    	// no storage ?
    	int noOfFiles = (int)getLongFromHeaderMemoryRegion( aMapHeaderFieldAddressNoOfMemoryPages );
    	// cannot expand => evict
    	if ( noOfFiles == maxBuffers )
    	{
    		return -1;
    	}
    	File file = new File( memoryMappedFilenamePrefix + "." + noOfFiles ); // record file
    	try
    	{
	    	memoryRegions[noOfFiles] = new StraddleAtomicBuffer( createNewMemoryMappedFileUnsafeBuffer( file, noOfFiles ) );
		    if( noOfFiles > 0 )
		    {
		    	memoryRegions[noOfFiles-1].setSecondBuffer(memoryRegions[noOfFiles].getFirstBuffer());
		    }
	    	noOfFiles += 1;
	    	putLongInHeaderMemoryRegion( aMapHeaderFieldAddressNoOfMemoryPages, noOfFiles );
	    	return noOfFiles * MAX_FILE_SIZE;
    	}
    	catch( IOException e )
    	{
    		return -1;
    	}
    }
    
    @Override
    public void dispose( boolean shouldEraseAllPersistedMemory )
    {
		if ( shouldEraseAllPersistedMemory )
		{
			for( File file: memoryMappedFileReferences )
			{
				if ( file != null )
				{
					file.delete();
				}
			}
		}
    }
}
