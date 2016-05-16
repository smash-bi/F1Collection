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

public class TestDataAccessPerformanceTest 
{
	public static void main( String[] args )
	{
		TestDataForF1BinaryMap binaryMapData = new TestDataForF1BinaryMap();
		binaryMapData.setData( 100000, 200000 );
		TestDataForJavaMap javaMapData = new TestDataForJavaMap( 10 );
		javaMapData.setData( 100000, 200000 );
		for( int count=0; count< 10000000; count++ )
		{
			binaryMapData.isCorrect();
		}
		for( int count=0; count< 10000000; count++ )
		{
			javaMapData.isCorrect();
		}
		long time = System.currentTimeMillis();
		for( int count=0; count< 90000000; count++ )
		{
			javaMapData.isCorrect();
		}
		time = System.currentTimeMillis() - time;
		System.out.println( "Java Map Data Access " + time );
		time = System.currentTimeMillis();
		for( int count=0; count< 90000000; count++ )
		{
			binaryMapData.isCorrect();
		}
		time = System.currentTimeMillis() - time;
		System.out.println( "Binary Map Data Access " + time );
	}
}
