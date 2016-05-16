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

/**
 * KeyValueToString converts key and value to human readable form
 */
public interface KeyValueToString 
{
	/**
	 * convert key to string
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key 
	 * @param aKeyLength length of the key
	 * @return human readable string of the key 
	 */
	public String convertKey( LongDirectBuffer aKey, long aKeyStartIndex, int aKeyLength );
	
	/**
	 * convert value to string
	 * @param aValue value
	 * @param aValueStartIndex start index of the value 
	 * @param aKeyLength length of the key
	 * @return human readable string of the value 
	 */
	public String convertValue( LongDirectBuffer aValue, long aValueStartIndex, int aKeyLength );
}
