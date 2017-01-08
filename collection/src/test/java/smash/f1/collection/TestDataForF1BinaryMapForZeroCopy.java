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

public final class TestDataForF1BinaryMapForZeroCopy implements TestData
{
	public final static int KEY_SIZE = Long.BYTES*2;
	public final static int VALUE_SIZE = Long.BYTES*4;
	public final static int KEY_START_INDEX = 0;
	public final static int VALUE_START_INDEX = 0;
	private LongUnsafeBuffer keyBuffer = new LongUnsafeBuffer( new byte[KEY_SIZE] );
	private LongUnsafeBuffer valueBuffer = new LongUnsafeBuffer( 0, 0 );
	
	/**
	 * set key
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setKey( final long aKey1, final long aKey2 )
	{
		keyBuffer.putLong(0, aKey1);
		keyBuffer.putLong(8, aKey2);
	}
	
	/**
	 * set data
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setData( final long aKey1, final long aKey2 )
	{
		keyBuffer.putLong(0, aKey1);
		keyBuffer.putLong(8, aKey2);
		valueBuffer.putLong(0, aKey1);
		valueBuffer.putLong(8, aKey2);
		valueBuffer.putLong(16, aKey2);
		valueBuffer.putLong(24, aKey1);
	}
	
	/**
	 * get key buffer
	 */
	public LongUnsafeBuffer getKeyBuffer()
	{
		return keyBuffer;
	}
	
	/**
	 * get value buffer
	 */
	public LongUnsafeBuffer getValueBuffer()
	{
		return valueBuffer;
	}
	
	/**
	 * check if the data is correct
	 */
	public boolean isCorrect()
	{
		return ( keyBuffer.getLong(0) == valueBuffer.getLong(0) && keyBuffer.getLong(0) == valueBuffer.getLong(24) ) 
				&&
				( keyBuffer.getLong(8) == valueBuffer.getLong(8) && keyBuffer.getLong(8) == valueBuffer.getLong(16) );
	}
	
	/**
	 * get key 1
	 * @return key 1
	 */
	public long getKey1()
	{
		return keyBuffer.getLong(0 );
	}
	
	/**
	 * get key 2
	 * @return key 2
	 */
	public long getKey2()
	{
		return keyBuffer.getLong(8 );
	}
	
	@Override
	public String getPrintableText(){
		StringBuilder builder = new StringBuilder();
		builder.append( keyBuffer.getLong(0) );
		builder.append( ' ' );
		builder.append( keyBuffer.getLong(8) );
		builder.append( ' ' );
		builder.append( valueBuffer.getLong(0) );
		builder.append( ' ' );
		builder.append( valueBuffer.getLong(8) );
		builder.append( ' ' );
		builder.append( valueBuffer.getLong(16) );
		builder.append( ' ' );
		builder.append( valueBuffer.getLong(24) );
		return builder.toString();
	}
}
