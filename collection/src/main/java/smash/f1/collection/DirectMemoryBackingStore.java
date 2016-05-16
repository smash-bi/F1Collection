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

import smash.f1.core.agrona.LongAtomicBuffer;
import smash.f1.core.agrona.LongDirectBuffer;
import smash.f1.core.agrona.LongMutableDirectBuffer;
import smash.f1.core.agrona.LongUnsafeBuffer;
import uk.co.real_logic.agrona.UnsafeAccess;

/**
 * DirectMemoryBackingStore uses direct volatile memory to serve as the backing store
 * for the F1 Binary Map
 */
final class DirectMemoryBackingStore implements MapBackingStore
{
	private final long mapSize;
	private final long startingAddress;
	private final LongUnsafeBuffer buffer;
	
	/**
	 * create direct memory backing store
	 */
	DirectMemoryBackingStore( final long aMaxMapSize )
	{
		mapSize = aMaxMapSize;
		startingAddress = UnsafeAccess.UNSAFE.allocateMemory(aMaxMapSize);
		buffer = new LongUnsafeBuffer(startingAddress, mapSize);
	}
	
	@Override
	public long getLongFromHeaderMemoryRegion(long anAddress) 
	{
		return buffer.getLong(anAddress);
	}

	@Override
	public long getLongVolatileFromHeaderMemoryRegion(long anAddress) 
	{
		return buffer.getLongVolatile(anAddress);
	}

	@Override
	public void putLongInHeaderMemoryRegion(long anAddress, long aNewValue) 
	{
		buffer.putLong(anAddress, aNewValue);
	}

	@Override
	public boolean compareAndPutLongInHeaderMemoryRegion(long anAddress,
			long aNewValue, long anOldValue) 
	{
		return buffer.compareAndSetLong( anAddress, anOldValue, aNewValue );
	}

	@Override
	public long getLongFromMemoryRegion(long anAddress) 
	{
		return buffer.getLong(anAddress);
	}

	@Override
	public void putLongInMemoryRegion(long anAddress, long aNewValue) 
	{
		buffer.putLong(anAddress, aNewValue);
	}

	@Override
	public void putLongInMemoryRegion(long anAddress, long aNewValue,
			long anOldValue) 
	{
    	while( !buffer.compareAndSetLong( anAddress, anOldValue, aNewValue ) )
    	{
    	}   	
	}

	@Override
	public void getBytesFromMemoryRegion(long anAddress, int aLength,
			LongMutableDirectBuffer aReceivingBuffer, long aReceivingBufferStartIndex) 
	{
		aReceivingBuffer.putBytes( aReceivingBufferStartIndex, buffer, anAddress, aLength );
	}

	@Override
	public void putBytesToMemoryRegion(long anAddress, int aLength,
			LongDirectBuffer aSourceBuffer, long aSourceBufferStartIndex) 
	{
		aSourceBuffer.getBytes( aSourceBufferStartIndex, buffer, anAddress, aLength );
	}

	@Override
	public long expand(long aMapHeaderFieldAddressNoOfMemoryPages) 
	{
		// cannot expand
		return -1;
	}

	@Override
	public LongAtomicBuffer getMemoryRegion(long anAddress) 
	{
		return buffer;
	}

	@Override
	public long getMemoryBufferAddress(long anAddress) 
	{
		return anAddress;
	}

	@Override
	public long calculateNoOfMemoryPages(long aMapSize) 
	{
		return 1;
	}

	@Override
	public void dispose(boolean shouldEraseAllPersistedMemory) 
	{
		UnsafeAccess.UNSAFE.freeMemory(startingAddress);
	}
}
