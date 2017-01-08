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

import smash.f1.core.agrona.LongUnsafeBuffer;

public final class TestDataForF1BinaryMap implements TestData
{
	public final static int KEY_SIZE = Long.BYTES*2;
	public final static int VALUE_SIZE = Long.BYTES*4;
	public final static int TOTAL_SIZE = KEY_SIZE + VALUE_SIZE;
	public final static int KEY_START_INDEX = 0;
	public final static int VALUE_START_INDEX = KEY_SIZE;
	private LongUnsafeBuffer buffer = new LongUnsafeBuffer( new byte[TOTAL_SIZE] );
	
	/**
	 * set key
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setKey( final long aKey1, final long aKey2 )
	{
		buffer.putLong(0, aKey1);
		buffer.putLong(8, aKey2);
		buffer.putLong(16, -1L);
		buffer.putLong(24, -1L);
		buffer.putLong(32, -1L);
		buffer.putLong(40, -1L);
	}
	
	/**
	 * set data
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setData( final long aKey1, final long aKey2 )
	{
		buffer.putLong(0, aKey1);
		buffer.putLong(8, aKey2);
		buffer.putLong(16, aKey1);
		buffer.putLong(24, aKey2);
		buffer.putLong(32, aKey2);
		buffer.putLong(40, aKey1);
	}
	
	/**
	 * get buffer
	 */
	public LongUnsafeBuffer getBuffer()
	{
		return buffer;
	}
	
	/**
	 * check if the data is correct
	 */
	public boolean isCorrect()
	{
		return ( buffer.getLong(0) == buffer.getLong(16) && buffer.getLong(0) == buffer.getLong(40) ) 
				&&
				( buffer.getLong(8) == buffer.getLong(24) && buffer.getLong(8) == buffer.getLong(32) );
	}
	
	public String toString()
	{
		return buffer.getLong(0 ) + ":" + buffer.getLong(8);
	}
	
	/**
	 * get key 1
	 * @return key 1
	 */
	public long getKey1()
	{
		return buffer.getLong(0 );
	}
	
	/**
	 * get key 2
	 * @return key 2
	 */
	public long getKey2()
	{
		return buffer.getLong(8 );
	}
	
	@Override
	public String getPrintableText(){
		StringBuilder builder = new StringBuilder();
		builder.append( buffer.getLong(0) );
		builder.append( ' ' );
		builder.append( buffer.getLong(8) );
		builder.append( ' ' );
		builder.append( buffer.getLong(16) );
		builder.append( ' ' );
		builder.append( buffer.getLong(24) );
		builder.append( ' ' );
		builder.append( buffer.getLong(32) );
		builder.append( ' ' );
		builder.append( buffer.getLong(40) );
		return builder.toString();
	}
}
