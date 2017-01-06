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

import smash.f1.core.agrona.LongUnsafeBuffer;

public final class TestDataMapForF1BinaryMap implements TestDataMap
{
	private F1BinaryMap binaryMap;
	
	public TestDataMapForF1BinaryMap( final long anInitialNoOfRecords, 
    		long aNoOfBuckets, long aMaxNoOfRecords,
    		boolean isConcurrentMap ) throws IOException
    {
		binaryMap = new F1BinaryMap( anInitialNoOfRecords, 
    		TestDataForF1BinaryMap.KEY_SIZE, TestDataForF1BinaryMap.VALUE_SIZE, aNoOfBuckets, aMaxNoOfRecords,
    		isConcurrentMap, new TestDataKeyFunction( aNoOfBuckets ) );
    }
	
	public TestDataMapForF1BinaryMap( final String aMemoryMappedFilenamePrefix, final long anInitialNoOfRecords, 
    		long aNoOfBuckets, long aMaxNoOfRecords,
    		boolean isConcurrentMap ) throws IOException
    {
		binaryMap = new F1BinaryMap( aMemoryMappedFilenamePrefix, anInitialNoOfRecords, 
    		TestDataForF1BinaryMap.KEY_SIZE, TestDataForF1BinaryMap.VALUE_SIZE, aNoOfBuckets, aMaxNoOfRecords,
    		isConcurrentMap, new TestDataKeyFunction( aNoOfBuckets ) );
    }
	
	/**
	 * put test data into the map
	 * @param aData
	 */
	public void put( final TestData aData )
	{
		LongUnsafeBuffer buffer = ((TestDataForF1BinaryMap)aData).getBuffer();
		binaryMap.put( buffer, TestDataForF1BinaryMap.KEY_START_INDEX, buffer, TestDataForF1BinaryMap.VALUE_START_INDEX );
	}
	
	/**
	 * get test data from the map
	 */
	public TestData get( final TestData aData )
	{
		LongUnsafeBuffer buffer = ((TestDataForF1BinaryMap)aData).getBuffer();
		if ( binaryMap.get(buffer, TestDataForF1BinaryMap.KEY_START_INDEX, buffer, TestDataForF1BinaryMap.VALUE_START_INDEX ) )
		{
			return aData;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * remove the test data from the map
	 */
	public boolean remove( final TestData aData )
	{
		return binaryMap.remove( ((TestDataForF1BinaryMap)aData).getBuffer(), TestDataForF1BinaryMap.KEY_START_INDEX );
	}
	
	/**
	 * get if the map contains the test data
	 */
	public boolean contains( final TestData aData )
	{
		return binaryMap.contains( ((TestDataForF1BinaryMap)aData).getBuffer(), TestDataForF1BinaryMap.KEY_START_INDEX );
	}
	
	/**
	 * dispose the map and releases all the resources
	 * @param shouldRemoveFiles true will remove all the existing memory mapped files from the system
	 */
	public void dispose( final boolean shouldRemoveFiles )
	{
		binaryMap.dispose(shouldRemoveFiles);
	}
	
	/**
	 * dump
	 */
	public void dump()
	{
		binaryMap.dump( new TestDataToString() );
	}
	
	/**
	 * statistics
	 */
	public F1BinaryMapStatistics statistics()
	{
		return binaryMap.statistics();
	}
	
	/**
	 * get size
	 * @return size of the map
	 */
	public long getSize()
	{
		return binaryMap.getSize();
	}
	
	/**
	 * get max size
	 * @return max size of the map
	 */
	public long getMaxSize()
	{
		return binaryMap.getMaxMapSize();
	}
	
	/**
	 * iterate all the key and values by traversing the map. This method can be used by non-concurrent map
	 * and memory access will be provided in no copy style. Access this method with concurrent map will get run time exception
	 * @param anIterator iterator
	 */
	public void traverse( final F1BinaryMapIterator anIterator )
	{
		binaryMap.traverse( anIterator );
	}
	
	/**
	 * iterate all long values of the given value offset by traversing the map. This method can be used by non-concurrent map
	 * and memory access will be provided in no copy style. Access this method with concurrent map will get run time exception
	 * @param anIterator iterator
	 * @param aValueOffset address offset from the value position where the long value is located in each record
	 */
	public void traverse( final F1BinaryMapLongValueIterator anIterator, final int aValueOffset )
	{
		binaryMap.traverse(anIterator, aValueOffset);
	}
	
	/**
	 * clear all the values from the map
	 */
	public void clear()
	{
		binaryMap.clear();
	}

	@Override
	public boolean getZeroCopy(final TestData aData ) 
	{
		LongUnsafeBuffer keyBuffer = ((TestDataForF1BinaryMapForZeroCopy)aData).getKeyBuffer();
		LongUnsafeBuffer valueBuffer = ((TestDataForF1BinaryMapForZeroCopy)aData).getValueBuffer();
		return binaryMap.getWithZeroCopy(keyBuffer, TestDataForF1BinaryMapForZeroCopy.KEY_START_INDEX, valueBuffer, TestDataForF1BinaryMapForZeroCopy.VALUE_START_INDEX );
	}

	@Override
	public TestData createTestData() 
	{
		return new TestDataForF1BinaryMap();
	}
	
	@Override
	public TestData createTestDataForZeroCopy()
	{
		return new TestDataForF1BinaryMapForZeroCopy();
	}

	@Override
	public boolean needNewData() 
	{
		return false;
	}

	@Override
	public void dispose() 
	{
		binaryMap.dispose(true);
	}
}
