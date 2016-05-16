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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import smash.f1.core.agrona.LongAtomicBuffer;
import smash.f1.core.agrona.LongDirectBuffer;
import smash.f1.core.agrona.LongMutableDirectBuffer;

/**
 * StraddleAtomicBuffer straddle with 2 atomic buffers
 */
final class StraddleAtomicBuffer implements LongAtomicBuffer
{
	// main buffer
	private final LongAtomicBuffer firstBuffer;
	// optional second buffer
	private LongAtomicBuffer secondBuffer;
	
	/**
	 * create a straddle atomic buffer
	 * @param aFirstBuffer first buffer
	 */
	StraddleAtomicBuffer( final LongAtomicBuffer aFirstBuffer )
	{
		firstBuffer = aFirstBuffer;
	}
	
	/**
	 * set the optional second buffer 
	 * @param aSecondBuffer second buffer
	 */
	void setSecondBuffer( final LongAtomicBuffer aSecondBuffer )
	{
		secondBuffer = aSecondBuffer;
	}
	
	/**
	 * get first buffer
	 * @return first buffer
	 */
	LongAtomicBuffer getFirstBuffer()
	{
		return firstBuffer;
	}
	
	/**
	 * get buffer based on the given index
	 * @param anIndex index of the address
	 * @return buffer corresponding buffer of the given address
	 */
	private LongAtomicBuffer getBuffer( final long anIndex )
	{
		if ( anIndex < firstBuffer.capacity() )
		{
			return firstBuffer;
		}
		else
		{
			return secondBuffer;
		}
	}
	
	/**
	 * get actual buffer address
	 * @param anIndex index of the address
	 * @return actual buffer address
	 */
	private long getIndex( final long anIndex )
	{
		if ( anIndex < firstBuffer.capacity() )
		{
			return anIndex;
		}
		else
		{
			return anIndex - firstBuffer.capacity();
		}
	}
	
	@Override
	public void setMemory(final long index, final int length, final byte value) 
	{
		getBuffer(index).setMemory(getIndex(index), length, value);
	}

	@Override
	public void putLong(final long index, final long value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putLong(getIndex(index), value, byteOrder);
	}

	@Override
	public void putLong(final long index, final long value) 
	{
		getBuffer(index).putLong(getIndex(index), value);
	}

	@Override
	public void putInt(final long index, final int value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putInt(getIndex(index), value, byteOrder);
	}

	@Override
	public void putInt(final long index, final int value) 
	{
		getBuffer(index).putInt(getIndex(index), value);
	}

	@Override
	public void putDouble(final long index, final double value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putDouble(getIndex(index), value,byteOrder);
	}

	@Override
	public void putDouble(final long index, final double value) 
	{
		getBuffer(index).putDouble(getIndex(index), value);
	}

	@Override
	public void putFloat(final long index, final float value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putFloat(getIndex(index), value, byteOrder);
	}

	@Override
	public void putFloat(final long index, final float value) 
	{
		getBuffer(index).putFloat(getIndex(index), value);
	}

	@Override
	public void putShort(final long index, final short value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putShort(getIndex(index), value, byteOrder);
	}

	@Override
	public void putShort(final long index, final short value) 
	{
		getBuffer(index).putShort(getIndex(index), value);
	}

	@Override
	public void putChar(final long index, final char value, final ByteOrder byteOrder) 
	{
		getBuffer(index).putChar(getIndex(index), value, byteOrder);
	}

	@Override
	public void putChar(final long index, final char value) 
	{
		getBuffer(index).putChar(getIndex(index), value);
	}

	@Override
	public void putByte(final long index, final byte value) 
	{
		getBuffer(index).putByte(getIndex(index), value);
	}

	@Override
	public void putBytes(final long index, final byte[] src) 
	{
		putBytes(index,src,0,src.length);
	}

	@Override
	public void putBytes(final long index, final byte[] src, final long offset, final int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.putBytes(index,src,offset,length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.putBytes(index,src,offset,firstFilled);
    		secondBuffer.putBytes(0, src,offset+firstFilled,overflow); 		
    	}	
	}

	@Override
	public void putBytes(final long index, final ByteBuffer srcBuffer, final int length) 
	{
		putBytes(index, srcBuffer, 0, length);
	}

	@Override
	public void putBytes(final long index, final ByteBuffer srcBuffer, final long srcIndex,
			int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.putBytes(index,srcBuffer,srcIndex,length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.putBytes(index,srcBuffer,srcIndex,firstFilled);
    		secondBuffer.putBytes(0, srcBuffer,srcIndex+firstFilled,overflow); 		
    	}			
	}

	@Override
	public void putBytes(final long index, final LongDirectBuffer srcBuffer, final long srcIndex,
			final int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.putBytes(index,srcBuffer,srcIndex,length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.putBytes(index,srcBuffer,srcIndex,firstFilled);
    		secondBuffer.putBytes(0, srcBuffer,srcIndex+firstFilled,overflow); 		
    	}		
	}

	@Override
	public int putStringUtf8(final long offset, final String value, final ByteOrder byteOrder) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int putStringUtf8(final long offset, final String value, final ByteOrder byteOrder,
			final int maxEncodedSize) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int putStringWithoutLengthUtf8(final long offset, final String value) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void wrap(final byte[] buffer) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final byte[] buffer, final long offset, final int length) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final ByteBuffer buffer) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final ByteBuffer buffer, final long offset, final int length) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final LongDirectBuffer buffer) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final LongDirectBuffer buffer, final long offset, final long length) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public void wrap(final long address, final long length) 
	{
		throw new RuntimeException( "This method is not supported" );
	}

	@Override
	public long addressOffset() 
	{
		return firstBuffer.addressOffset();
	}

	@Override
	public byte[] byteArray() 
	{
		return null;
	}

	@Override
	public ByteBuffer byteBuffer() 
	{
		return null;
	}

	@Override
	public long capacity() 
	{
		if ( secondBuffer != null )
		{
			return firstBuffer.capacity()+secondBuffer.capacity();
		}
		else
		{
			return firstBuffer.capacity();
		}
	}

	@Override
	public void checkLimit(final long limit) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public long getLong(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getLong(getIndex(index),byteOrder);
	}

	@Override
	public long getLong(final long index) 
	{
		return getBuffer(index).getLong(getIndex(index));
	}

	@Override
	public int getInt(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getInt(getIndex(index),byteOrder);
	}

	@Override
	public int getInt(final long index) 
	{
		return getBuffer(index).getInt(getIndex(index));
	}

	@Override
	public double getDouble(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getDouble(getIndex(index),byteOrder);
	}

	@Override
	public double getDouble(final long index) 
	{
		return getBuffer(index).getDouble(getIndex(index));
	}

	@Override
	public float getFloat(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getFloat(getIndex(index),byteOrder);
	}

	@Override
	public float getFloat(final long index) 
	{
		return getBuffer(index).getFloat(getIndex(index));
	}

	@Override
	public short getShort(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getShort(getIndex(index),byteOrder);
	}

	@Override
	public short getShort(final long index) 
	{
		return getBuffer(index).getShort(getIndex(index));
	}

	@Override
	public char getChar(final long index, final ByteOrder byteOrder) 
	{
		return getBuffer(index).getChar(getIndex(index),byteOrder);
	}

	@Override
	public char getChar(final long index) 
	{
		return getBuffer(index).getChar(getIndex(index));
	}

	@Override
	public byte getByte(final long index) 
	{
		return getBuffer(index).getByte(getIndex(index));
	}

	@Override
	public void getBytes(final long index, final byte[] dst) 
	{
		getBytes(index, dst, 0, dst.length);
	}

	@Override
	public void getBytes(final long index, final byte[] dst, final long offset, final int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.getBytes(index, dst, offset, length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.getBytes(index, dst, offset,firstFilled);
    		secondBuffer.getBytes(0, dst, offset+firstFilled, overflow); 		
    	}	
	}

	@Override
	public void getBytes(final long index, final LongMutableDirectBuffer dstBuffer,
			final long dstIndex, final int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.getBytes(index, dstBuffer, dstIndex, length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.getBytes(index, dstBuffer, dstIndex,firstFilled);
    		secondBuffer.getBytes(0, dstBuffer, dstIndex+firstFilled, overflow); 		
    	}
	}

	@Override
	public void getBytes(final long index, final ByteBuffer dstBuffer, final int length) 
	{
    	// if it is not straddling between 2 memory regions
    	int overflow = (int)(0 - ( firstBuffer.capacity() - index - length ));
    	if ( overflow <= 0 )
    	{
    		firstBuffer.getBytes(index, dstBuffer, length);
    	}
    	// if it is straddling between 2 memory regions
    	else
    	{
    		// take care of the first memory region
    		int firstFilled=length-overflow;
    		firstBuffer.getBytes(index, dstBuffer, firstFilled);
    		secondBuffer.getBytes(0, dstBuffer, overflow); 		
    	}
	}

	@Override
	public String getStringUtf8(final long offset, final ByteOrder byteOrder) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringUtf8(final long offset, final int length) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringWithoutLengthUtf8(final long offset, final int length) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void boundsCheck(final long index, final int length) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyAlignment() 
	{
		firstBuffer.verifyAlignment();
		if (secondBuffer != null)
		{
			secondBuffer.verifyAlignment();
		}
	}

	@Override
	public long getLongVolatile(final long index) 
	{
		return getBuffer(index).getLongVolatile(getIndex(index));
	}

	@Override
	public void putLongVolatile(final long index, final long value) 
	{
		getBuffer(index).putLongVolatile(getIndex(index), value);
	}

	@Override
	public void putLongOrdered(final long index, final long value) 
	{
		getBuffer(index).putLongOrdered(getIndex(index), value);
	}

	@Override
	public long addLongOrdered(final long index, final long increment) 
	{
		return getBuffer(index).addLongOrdered(getIndex(index), increment);
	}

	@Override
	public boolean compareAndSetLong(final long index, final long expectedValue,
			final long updateValue) {
		return getBuffer(index).compareAndSetLong(getIndex(index), expectedValue, updateValue);
	}

	@Override
	public long getAndSetLong(final long index, final long value) 
	{
		return getBuffer(index).getAndSetLong(getIndex(index), value);
	}

	@Override
	public long getAndAddLong(final long index, final long delta) 
	{
		return getBuffer(index).getAndAddLong(getIndex(index), delta);
	}

	@Override
	public int getIntVolatile(final long index) 
	{
		return getBuffer(index).getIntVolatile(getIndex(index));
	}

	@Override
	public void putIntVolatile(final long index, final int value) 
	{
		getBuffer(index).putIntVolatile(getIndex(index), value); 
	}

	@Override
	public void putIntOrdered(final long index, final int value) 
	{
		getBuffer(index).putIntOrdered(getIndex(index), value); 
	}

	@Override
	public int addIntOrdered(final long index, final int increment) 
	{
		return getBuffer(index).addIntOrdered(getIndex(index), increment);
	}

	@Override
	public boolean compareAndSetInt(final long index, final int expectedValue,
			int updateValue) 
	{
		return getBuffer(index).compareAndSetInt(getIndex(index), expectedValue, updateValue);
	}

	@Override
	public int getAndSetInt(final long index, final int value) 
	{
		return getBuffer(index).getAndSetInt(getIndex(index), value);
	}

	@Override
	public int getAndAddInt(final long index, final int delta) 
	{
		return getBuffer(index).getAndAddInt(getIndex(index), delta);
	}

	@Override
	public short getShortVolatile(final long index) 
	{
		return getBuffer(index).getShortVolatile(getIndex(index));
	}

	@Override
	public void putShortVolatile(final long index, final short value) 
	{
		getBuffer(index).putShortVolatile(getIndex(index), value);
	}

	@Override
	public char getCharVolatile(long index) 
	{
		return getBuffer(index).getCharVolatile(getIndex(index));
	}

	@Override
	public void putCharVolatile(long index, char value) 
	{
		getBuffer(index).putCharVolatile(getIndex(index), value);
	}

	@Override
	public byte getByteVolatile(long index) 
	{
		return getBuffer(index).getByteVolatile(getIndex(index));
	}

	@Override
	public void putByteVolatile(long index, byte value) 
	{
		getBuffer(index).putByteVolatile(getIndex(index), value);
	}
}
