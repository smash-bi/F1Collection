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
 * UUIDHashFunction provides hashing on UUID value
 */
public final class UUIDKeyFunction implements KeyFunction
{

	@Override
	public long hash(final LongDirectBuffer aValue, final long aStartIndex, final int aLength ) 
	{
		long time = aValue.getLong( aStartIndex );
		long clockSequenceAndNode = aValue.getLong( aStartIndex + Long.BYTES );
		return ((time >> 32) ^ time ^ (clockSequenceAndNode >> 32) ^ clockSequenceAndNode);
	}

	@Override
	public boolean equals(LongDirectBuffer aKey1, long aKey1StartIndex,
			LongDirectBuffer aKey2, long aKey2StartIndex, int aLength) 
	{
		return aKey1.getLong(aKey1StartIndex) == aKey2.getLong(aKey2StartIndex)
				&&
				aKey1.getLong(aKey1StartIndex+8) == aKey2.getLong(aKey2StartIndex+8);
	}
}
