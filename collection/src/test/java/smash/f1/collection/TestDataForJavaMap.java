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

public final class TestDataForJavaMap implements TestData
{
	private TestDataUUID uuid;
	private long value1;
	private long value2;
	private long value3;
	private long value4;
	
	public TestDataForJavaMap( int aNoOfBuckets )
	{
		uuid = new TestDataUUID( 0, 0, aNoOfBuckets );
	}
	
	public TestDataUUID getKey()
	{
		return uuid;
	}
	
	@Override
	public void setKey(final long aKey1, final long aKey2) 
	{
		uuid.setUUID(aKey1, aKey2);
		value1 = -1L;
		value2 = -1L;
		value3 = -1L;
		value4 = -1L;	
	}

	@Override
	public void setData(final long aKey1, final long aKey2) 
	{
		uuid.setUUID(aKey1, aKey2);
		value1 = aKey1;
		value2 = aKey2;
		value3 = aKey2;
		value4 = aKey1;			
	}

	@Override
	public boolean isCorrect() 
	{
		return ( uuid.getMostSignificantBits() == value1 && uuid.getMostSignificantBits() == value4 ) 
				&&
				( uuid.getLeastSignificantBits() == value2 && uuid.getLeastSignificantBits() == value3 );
	}

	@Override
	public long getKey1() 
	{
		return uuid.getMostSignificantBits();
	}

	@Override
	public long getKey2() 
	{
		return uuid.getLeastSignificantBits();
	}
	
	@Override
	public String getPrintableText(){
		StringBuilder builder = new StringBuilder();
		builder.append( uuid.getMostSignificantBits() );
		builder.append( ' ' );
		builder.append( uuid.getLeastSignificantBits() );
		builder.append( ' ' );
		builder.append( value1 );
		builder.append( ' ' );
		builder.append( value2 );
		builder.append( ' ' );
		builder.append( value3 );
		builder.append( ' ' );
		builder.append( value4 );
		return builder.toString();
	}
}
