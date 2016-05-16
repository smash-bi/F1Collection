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
 * F1BinaryMapIterator provides iterator interface to iterate through all 
 * the records in the map
 */
public interface F1BinaryMapIterator 
{
	/**
	 * iterate with given record
	 * @param aRecord record buffer
	 * @param aKeyStartIndex key start index of the record 
	 * @param aKeyLength length of the key
	 * @param aValueStartIndex value start index of the record
	 * @param aValueLength length of the value 
	 */
	public void iterate( LongDirectBuffer aRecord, long aKeyStartIndex, int aKeyLength, long aValueStartIndex, int aValueLength );
}
