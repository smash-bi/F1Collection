package smash.f1.collection;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;

public class TestDataKeyForChronicleMap implements Byteable
{
	private final static long	MAX_SIZE = 16;
	private BytesStore bytesStore;
	private long offset;
	
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
	
	/**
	 * set key
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setKey( long aKey1, long aKey2 )
	{
		bytesStore.writeLong( offset, aKey1 );
		bytesStore.writeLong( offset + 8, aKey2 );
	}

	/**
	 * get key 1
	 * @return key 1
	 */
	public long getKey1()
	{
		return bytesStore.readLong( offset );
	}
	
	/**
	 * get key 2
	 * @return key 2
	 */
	public long getKey2()
	{
		return bytesStore.readLong( offset + 8 );
	}
}
