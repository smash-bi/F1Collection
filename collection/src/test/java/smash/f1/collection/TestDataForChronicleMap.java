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

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;

public final class TestDataForChronicleMap implements TestData, Byteable
{
	private final static long	MAX_SIZE = 48;
	private BytesStore bytesStore;
	private long offset;
	
	public TestDataForChronicleMap()
	{
		Thread.currentThread().dumpStack();
	}
	
	@Override
	public BytesStore bytesStore() 
	{
		return bytesStore;
	}

	@Override
	public void bytesStore(BytesStore aBytesStore, long anOffset, long aSize)
			throws IllegalStateException, IllegalArgumentException,
			BufferOverflowException, BufferUnderflowException 
	{
		if ( aSize != MAX_SIZE )
		{
			throw new IllegalArgumentException();
		}
		bytesStore = aBytesStore;
		offset = anOffset;
	}

	@Override
	public long maxSize() 
	{
		return MAX_SIZE;
	}

	@Override
	public long offset() 
	{
		return offset;
	}

	@Override
	public void setKey(long aKey1, long aKey2) 
	{
		bytesStore.writeLong(offset+0, aKey1);
		bytesStore.writeLong(offset+8, aKey2);
		bytesStore.writeLong(offset+16, -1L);
		bytesStore.writeLong(offset+24, -1L);
		bytesStore.writeLong(offset+32, -1L);
		bytesStore.writeLong(offset+40, -1L);
	}

	@Override
	public void setData(long aKey1, long aKey2) 
	{
		bytesStore.writeLong(offset+0, aKey1);
		bytesStore.writeLong(offset+8, aKey2);
		bytesStore.writeLong(offset+16, aKey1);
		bytesStore.writeLong(offset+24, aKey2);
		bytesStore.writeLong(offset+32, aKey2);
		bytesStore.writeLong(offset+40, aKey1);
	}

	@Override
	public boolean isCorrect() 
	{
		return ( bytesStore.readLong(offset+0) ==  bytesStore.readLong(offset+16) &&  bytesStore.readLong(offset+0) ==  bytesStore.readLong(offset+40) ) 
				&&
				(  bytesStore.readLong(offset+8) ==  bytesStore.readLong(offset+24) &&  bytesStore.readLong(offset+8) ==  bytesStore.readLong(offset+32) );
	}

	@Override
	public long getKey1() 
	{
		return bytesStore.readLong(offset+0 );
	}

	@Override
	public long getKey2() 
	{
		return bytesStore.readLong(offset+8 );
	}

	@Override
	public String getPrintableText() 
	{
		StringBuilder builder = new StringBuilder();
		builder.append( bytesStore.readLong(offset+0) );
		builder.append( ' ' );
		builder.append( bytesStore.readLong(offset+8) );
		builder.append( ' ' );
		builder.append( bytesStore.readLong(offset+16) );
		builder.append( ' ' );
		builder.append( bytesStore.readLong(offset+24) );
		builder.append( ' ' );
		builder.append( bytesStore.readLong(offset+32) );
		builder.append( ' ' );
		builder.append( bytesStore.readLong(offset+40) );
		return builder.toString();
	}
	
}
