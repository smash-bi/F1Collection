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

public class TestDataToString implements KeyValueToString
{

	@Override
	public String convertKey(LongDirectBuffer aKey, long aKeyStartIndex,
			int aKeyLength) 
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( aKey.getLong( aKeyStartIndex) );
		buffer.append( '-' );
		buffer.append( aKey.getLong( aKeyStartIndex + Long.BYTES ) );
		return buffer.toString();
	}

	@Override
	public String convertValue(LongDirectBuffer aValue, long aValueStartIndex,
			int aValueLength) 
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append( aValue.getLong( aValueStartIndex) );
		buffer.append( '>' );
		buffer.append( aValue.getLong( aValueStartIndex + Long.BYTES ) );
		buffer.append( '>' );
		buffer.append( aValue.getLong( aValueStartIndex + Long.BYTES * 2 ) );
		buffer.append( '>' );
		buffer.append( aValue.getLong( aValueStartIndex + Long.BYTES * 3 ) );
		return buffer.toString();
	}

}
