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

import java.util.concurrent.atomic.AtomicBoolean;

import uk.co.real_logic.agrona.concurrent.SigInt;

public final class F1BinaryMapConcurrentTestHarness implements Runnable
{
	private final static AtomicBoolean DISPOSED = new AtomicBoolean(false);
	
	private final TestDataMapForF1BinaryMap map;
	private final long noOfData;
	private final int identifier;
	
	/**
	 * create test harness
	 */
	public F1BinaryMapConcurrentTestHarness( final TestDataMapForF1BinaryMap aMap, final long aNoOfData, final int anIdentifier )
	{
		map = aMap;
		noOfData = aNoOfData;
		identifier = anIdentifier;
	}
	
	/**
	 * prepare the test
	 * @param aMap map to be used
	 * @param aNoOfData no of data to be used for testing
	 */
	public final void run()
	{
		final TestData data = map.createTestData();
		long time = 0;
		while(!DISPOSED.get())
		{
			System.out.println( "Starting Loop " + identifier );
			time = System.currentTimeMillis();
			for( long count=0; count<noOfData; count++ )
			{
				data.setData( count, count+noOfData);
				map.put(data);
				TestData retrievedData = map.getWithVerification(data);
				if ( retrievedData != null && ( !retrievedData.isCorrect() || retrievedData.getKey1() != count || retrievedData.getKey2() != (count+noOfData) ) )
				{
					System.err.println( "Data is incorrect " + count + " expecting " + count + " " + count+noOfData + " got " + retrievedData.getPrintableText());
				}
			}
			time = System.currentTimeMillis() - time;
			System.out.println( "Completed 1 Loop " + identifier + " Time " + time );
			//map.clear();
		}
		System.out.println( "Done " + identifier );
	}

	public static void main(String[] args )
	{
		TestDataMapForF1BinaryMap map = null;
        SigInt.register( ()-> { DISPOSED.set(true); } );
		try
		{
			long noOfData = Long.parseLong( args[0] );
			String mapClass = args[1];
			String mapFileDirectory = args[2];
			int noOfConcurrentTests = Integer.parseInt( args[3] );
			if ( mapClass.equals( "F1BinaryMap") )
			{
				TestDataMapForF1BinaryMap binaryMap = new TestDataMapForF1BinaryMap( mapFileDirectory, noOfData, 
								noOfData, noOfData, true );
				map = binaryMap;
				System.out.println( "Max Size " + binaryMap.getMaxSize() + " Size " + binaryMap.getSize()  );
			}
			else if ( mapClass.equals( "F1BinaryMapDirect"))
			{
				TestDataMapForF1BinaryMap binaryMap = new TestDataMapForF1BinaryMap( noOfData, 
						noOfData, noOfData, true );
				map = binaryMap;
				System.out.println( "Max Size " + binaryMap.getMaxSize() + " Size " + binaryMap.getSize()  );
			}
			else
			{
				throw new RuntimeException( "No such map type " + mapClass );
			}
			map.clear();
			for( int count=0; count<noOfConcurrentTests; count++ )
			{
				Thread thread = new Thread( new F1BinaryMapConcurrentTestHarness( map, noOfData, count ) );
				thread.start();
				System.out.println( "Started " + count );
			}
		}
		catch( Throwable t )
		{
			t.printStackTrace();
		}
	}
}
