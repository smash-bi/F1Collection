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
 * KeyFunction allows implementation to provide hashing and equal functions to handle 
 * a specific type of keys
 */
public interface KeyFunction 
{
	/**
	 * calculate the hash for the given data
	 * @param aKey key to be used to calculate the hash
	 * @param aStartIndex starting index of the key
	 * @param aLength length of the value
	 * @return hash value of the key
	 */
	public long hash( LongDirectBuffer aKey, long aStartIndex, int aLength );
	
	/**
	 * check if the 2 given keys are equals
	 * @param aKey1 first key in the comparison
	 * @param aKey1StartIndex starting index of the first key
	 * @param aKey2 second key in the comparison
	 * @param aKey2StartIndex starting 
	 * @param aLength length of the value
	 * @return true if 2 given keys are the same or false if 2 given keys are different
	 */
	public boolean equals( LongDirectBuffer aKey1, long aKey1StartIndex, LongDirectBuffer aKey2, long aKey2StartIndex, int aLength );
}
