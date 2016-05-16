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

public class F1BinaryMapConcurrentTestHarness 
{
	public static void main( String[] args )
	{
		long noOfItems = 1_000_000L;
		TestDataMapForF1BinaryMap map = null;
		try
		{
			map = new TestDataMapForF1BinaryMap( "TestDataMap", noOfItems, noOfItems, noOfItems, true ); 
			TestDataForF1BinaryMap data = new TestDataForF1BinaryMap();
			for( int count=0; count<2; count++ )
			{
				data.setData( 0, 1 );
				map.put( data );
				map.get( data );
				map.remove( data );
				map.clear();
			}
		}
		catch( Throwable e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				map.dispose( true );
			}
			catch( Throwable t )
			{}
		}
	}
}
