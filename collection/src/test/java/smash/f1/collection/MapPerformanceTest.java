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

import java.util.ArrayList;

public final class MapPerformanceTest 
{
	private final static ArrayList<TestData> TestDataList = new ArrayList<TestData>();
	
	/**
	 * prepare the test
	 * @param aMap map to be used
	 * @param aNoOfData no of data to be used for testing
	 */
	public final static void PrepareTest( final TestDataMap aMap, final long aNoOfData )
	{
		long time = System.currentTimeMillis();
		aMap.clear();
		time = System.currentTimeMillis() - time;
		System.out.println( "Prepare Test Clear Took " + time );
		time = System.currentTimeMillis();
		TestData data = null;
		for( long count=0; count<aNoOfData; count++ )
		{
			if ( data == null || aMap.needNewData() )
			{
				data = aMap.createTestData();
				if ( aMap.needNewData() )
				{
					TestDataList.add( data );
				}
			}
			data.setData( count, count+aNoOfData);
			aMap.put(data);
		}
		time = System.currentTimeMillis() - time;
		System.out.println( "Prepare Test Put Took " + time );
		time = System.currentTimeMillis();
		if ( aMap.needNewData() )
		{
			data = aMap.createTestData();
		}
		for( long count=0; count<aNoOfData; count++ )
		{
			data.setKey(count, count+aNoOfData);
			TestData retrievedData = aMap.get(data);
			if ( retrievedData == null )
			{
				throw new RuntimeException( "Data is missing " + count );
			}
			if ( !retrievedData.isCorrect() )
			{
				throw new RuntimeException( "Data is incorrect " + count + " expecting " + count + " " + count+aNoOfData + " got " + retrievedData.getPrintableText());
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println( "Prepare Test Get Took " + time );
		time = System.currentTimeMillis();
		aMap.clear();
		time = System.currentTimeMillis() - time;
		System.out.println( "Prepare Test Second Clear Took " + time );
	}
	
	/**
	 * test add
	 * @param aMap map to be used
	 * @param aNoOfData no of data to be used for testing
	 * @return time taken in ms
	 */
	public final static long TestAdd( final TestDataMap aMap, final long aNoOfData )
	{
		long time = System.currentTimeMillis();
		TestData data = null;
		for( long count=0; count<aNoOfData; count++ )
		{
			if ( aMap.needNewData() )
			{
				data = TestDataList.get( (int)count );
			}
			else if ( data == null )
			{
				data = aMap.createTestData();
			}
			data.setData( count, count+aNoOfData);
			aMap.put(data);
		}
		return System.currentTimeMillis() - time;
	}
	
	/**
	 * test get
	 * @param aMap map to be used
	 * @param aNoOfData no of data to be used for testing
	 * @return time taken in ms
	 */
	public final static long TestGet( final TestDataMap aMap, final long aNoOfData )
	{
		long time = System.currentTimeMillis();
		TestData data = aMap.createTestData();
		for( long count=0; count<aNoOfData; count++ )
		{
			data.setKey(count, count+aNoOfData);
			TestData retrievedData = aMap.get(data);
			if ( !retrievedData.isCorrect() || retrievedData.getKey1() != count || retrievedData.getKey2() != (count+aNoOfData)  )
			{
				throw new RuntimeException( "Data is incorrect " + count );
			}
		}
		return System.currentTimeMillis() - time;
	}
	
	/**
	 * test get with zero copy
	 * @param aMap map to be used
	 * @param aNoOfData no of data to be used for testing
	 * @return time taken in ms
	 */
	public final static long TestGetWithZeroCopy( final TestDataMap aMap, final long aNoOfData )
	{
		long time = System.currentTimeMillis();
		TestData data = aMap.createTestDataForZeroCopy();
		if ( data == null )
		{
			// not supporting zero copy
			return -1;
		}
		for( long count=0; count<aNoOfData; count++ )
		{
			data.setKey(count, count+aNoOfData);
			if ( !aMap.getZeroCopy(data) )
			{
				throw new RuntimeException( "Data does not exist " + count );
			}
			if ( !data.isCorrect() || data.getKey1() != count || data.getKey2() != (count+aNoOfData) )
			{
				System.out.println( "Incorrect data " + data.getPrintableText() );
				throw new RuntimeException( "Data is incorrect " + count );
			}
		}
		return System.currentTimeMillis() - time;
	}
	
	public static void main(String[] args )
	{
		TestDataMap map = null;
		try
		{
			long noOfData = Long.parseLong( args[0] );
			String mapClass = args[1];
			String mapFileDirectory = args[2];
			if ( mapClass.equals( "HashMap" ) )
			{
				if ( noOfData > Integer.MAX_VALUE )
				{
					map = new TestDataMapForHashMap( Integer.MAX_VALUE );
				}
				else
				{
					map = new TestDataMapForHashMap( (int)noOfData );
				}
			}
			else if ( mapClass.equals( "ConcurrentHashMap" ) )
			{
				if ( noOfData > Integer.MAX_VALUE )
				{
					map = new TestDataMapForConcurrentHashMap( Integer.MAX_VALUE );
				}
				else
				{
					map = new TestDataMapForConcurrentHashMap( (int)noOfData );
				}
			}
			else if ( mapClass.equals( "F1BinaryMap") )
			{
				TestDataMapForF1BinaryMap binaryMap = new TestDataMapForF1BinaryMap( mapFileDirectory, noOfData, 
								noOfData, noOfData, false );
				map = binaryMap;
				System.out.println( "Max Size " + binaryMap.getMaxSize() + " Size " + binaryMap.getSize()  );
			}
			else if ( mapClass.equals( "F1BinaryMapDirect"))
			{
				TestDataMapForF1BinaryMap binaryMap = new TestDataMapForF1BinaryMap( noOfData, 
						noOfData, noOfData, false );
				map = binaryMap;
				System.out.println( "Max Size " + binaryMap.getMaxSize() + " Size " + binaryMap.getSize()  );
			}
			else if ( mapClass.equals( "ChronicleMap"))
			{
				TestDataMapForChronicleMap chronicleMap = new TestDataMapForChronicleMap( noOfData );
				map = chronicleMap;
				System.out.println( "Off Heap Size " + chronicleMap.getOffHeapMemory()  );
			}
			else
			{
				throw new RuntimeException( "No such map type " + mapClass );
			}
			System.out.println( "Warming up " + mapClass + " Testing " + noOfData + " data" );
			PrepareTest( map, noOfData );
			for( int count=0; count<20; count++ )
			{
				System.out.println( "Test Put " + TestAdd( map, noOfData ) );
				System.out.println( "Test Get " + TestGet( map, noOfData ) );
				if ( map.isZeroCopyGetAllowed() )
				{
					System.out.println( "Test Zero Copy " + TestGetWithZeroCopy( map, noOfData ) );
				}
			}
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
		finally
		{
			map.dispose();
		}
	}
}
