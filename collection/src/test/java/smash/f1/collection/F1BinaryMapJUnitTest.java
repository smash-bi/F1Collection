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

import smash.f1.core.agrona.LongDirectBuffer;
import junit.framework.TestCase;

public class F1BinaryMapJUnitTest extends TestCase implements F1BinaryMapIterator, F1BinaryMapLongValueIterator
{
	private TestDataMapForF1BinaryMap map;
	private final long noOfItems = 1_000_000L;
	private long iterationCount = 0L;
	private long totalLongValue = 0L;
	private long sumForthField = 0L;
	private TestDataForF1BinaryMap data = new TestDataForF1BinaryMap();
	private TestDataForF1BinaryMapForZeroCopy dataForZeroCopy = new TestDataForF1BinaryMapForZeroCopy();
	private long size = 0L;
	
	protected void setUp() throws Exception 
	{
		super.setUp();
		//map = new TestDataMapForF1BinaryMap( "TestDataMap", noOfItems, noOfItems, noOfItems, false );
		map = new TestDataMapForF1BinaryMap( noOfItems, noOfItems, noOfItems, false );
	}

	protected void tearDown() throws Exception 
	{
		/*
		F1BinaryMapStatistics statistics = map.statistics();
		System.out.println( "Statistics " );
		System.out.println( "============================" );
		System.out.println( "No Of Bucket Used " + statistics.getNoOfBucketsUsed() );
		System.out.println( "Max Chain Length " + statistics.getMaxChainLength() );
		System.out.println( "No of buckets with max chain length " + statistics.getNoOfBucketsWithMaxChainLength() );
		if ( statistics.getNoOfBucketsUsed() != 0 )
		{
			System.out.println( "Average Chain size " + (double)statistics.getSize() / statistics.getNoOfBucketsUsed() );
		}
		System.out.println( "No of expansions " + statistics.getNoOfExpansions() );
		System.out.println( "No of evictions " + statistics.getNoOfEvictions() );
		System.out.println( "No of free list " + statistics.getNoOfFreeLists() );
		System.out.println( "Size of Map " + statistics.getSize() );
		*/
		size = 0;
		sumForthField = 0;
		super.tearDown();
		map.clear();
		map.dispose( true );
		map = null;
	}

	public void testAdd()
	{
		TestData retrievedData = null;
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(key, key+noOfItems);
			map.put( data ); 
			sumForthField += data.getBuffer().getLong(40);
			size++;
		}
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setKey(key, key+noOfItems);
			retrievedData = map.get( data );
			assertEquals( "Data " + key + " does not exist", true, retrievedData != null );
			if ( retrievedData != null )
			{
				assertEquals( "Data " + key + " does not contain the right data", true, retrievedData.isCorrect() );
			}
		}
		for( long key = 0L; key < noOfItems; key++ )
		{
			dataForZeroCopy.setKey(key, key+noOfItems);
			boolean exist = map.getZeroCopy( dataForZeroCopy );
			assertEquals( "Data with zero copy " + key + " does not exist", true, exist );
			if ( exist )
			{
				assertEquals( "Data with zero copy " + key + " does not contain the right data", true, dataForZeroCopy.isCorrect() );
			}
		}
		assertEquals( "Size is wrong " + map.getSize() + " " + size, size, map.getSize() );
	}
	
	public void testRemove()
	{
		long currentIndex = noOfItems;
		long data1Key1 = currentIndex++;
		long data1Key2 = data1Key1 + 1000L;
		long data2Key1 = currentIndex++;
		long data2Key2 = data1Key1 + 1000L;
		long data3Key1 = currentIndex++;
		long data3Key2 = data1Key1 + 1000L;
		data.setData(data1Key1, data1Key2);
		map.put( data );
		sumForthField += data.getBuffer().getLong(40);
		size++;
		data.setData(data2Key1, data2Key2);
		map.put( data );
		sumForthField += data.getBuffer().getLong(40);
		size++;
		data.setData(data3Key1, data3Key2);
		map.put( data );
		sumForthField += data.getBuffer().getLong(40);
		size++;
		data.setData(data1Key1, data1Key2);
		map.remove( data );
		sumForthField -= data.getBuffer().getLong(40);
		size--;
		boolean firstDataExists = map.contains( data );
		data.setKey(data2Key1, data2Key2);
		boolean secondDataExists = map.contains( data );
		data.setKey(data3Key1, data3Key2);
		boolean thirdDataExists = map.contains( data );
		assertEquals( "First data was not removed", false, firstDataExists );
		assertEquals( "Second data got removed", true, secondDataExists );
		assertEquals( "Third data got removed", true, thirdDataExists );
		assertEquals( "Size is wrong " + map.getSize() + " " + size, size, map.getSize() );
	}
	
	public void testDuplicateAdd()
	{
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(0, 0+noOfItems);
			map.put( data );
		}
		assertEquals( "Size is wrong " + map.getSize(), 1, map.getSize() );
	}
	
	public void testIterateCount()
	{
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(key, key+noOfItems);
			map.put( data ); 
			sumForthField += data.getBuffer().getLong(40);
			size++;
		}
		iterationCount = 0L;
		map.traverse( this );
		assertEquals( "Wrong iteration count got " + iterationCount + " expected " + size, size, iterationCount );
	}
	
	public void testIterateSumForthField()
	{
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(key, key+noOfItems);
			map.put( data ); 
			sumForthField += data.getBuffer().getLong(40);
			size++;
		}
		totalLongValue = 0;
		map.traverse( this, 24 );
		assertEquals( "Wrong iteration sum forth field got " + totalLongValue + " expected " + sumForthField, sumForthField, totalLongValue );
	}
	
	public void testClear()
	{
		TestData retrievedData = null;
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(key, key+noOfItems);
			map.put( data ); 
			sumForthField += data.getBuffer().getLong(40);
			size++;
		}
		map.clear();
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setData(key, key+noOfItems);
			map.put( data ); 
			sumForthField += data.getBuffer().getLong(40);
			size++;
		}
		for( long key = 0L; key < noOfItems; key++ )
		{
			data.setKey(key, key+noOfItems);
			retrievedData = map.get( data );
			assertEquals( "Data " + key + " does not exist", true, retrievedData != null );
			if ( retrievedData != null )
			{
				assertEquals( "Data " + key + " does not contain the right data", true, retrievedData.isCorrect() );
			}
		}
		map.clear();
		assertEquals( "Size is wrong " + map.getSize(), map.getSize(), 0 );
		F1BinaryMapStatistics statistics = map.statistics();
		assertEquals( "Clear did not clear all items with evictions " + statistics.getNoOfEvictions(), 0, statistics.getNoOfEvictions() );
	}
	
	@Override
	public void iterate(LongDirectBuffer aRecord, long aKeyStartIndex,
			int aKeyLength, long aValueStartIndex, int aValueLength) 
	{
		iterationCount++;
	}
	

	@Override
	public void iterate(long aLongValue) 
	{
		totalLongValue += aLongValue;
	}
}
