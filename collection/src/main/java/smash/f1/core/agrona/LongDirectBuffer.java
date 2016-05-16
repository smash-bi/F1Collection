/*
 * Copyright 2014 - 2016 Real Logic Ltd.
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
package smash.f1.core.agrona;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Abstraction over a range of buffer types that allows fields to be read in native typed fashion.
 */
public interface LongDirectBuffer
{
    /**
     * Attach a view to a byte[] for providing direct access.
     *
     * @param buffer to which the view is attached.
     */
    void wrap(byte[] buffer);

    /**
     * Attach a view to a byte[] for providing direct access.
     *
     * @param buffer to which the view is attached.
     * @param offset at which the view begins.
     * @param length of the buffer included in the view
     */
    void wrap(byte[] buffer, long offset, int length);

    /**
     * Attach a view to a {@link ByteBuffer} for providing direct access, the {@link ByteBuffer} can be
     * heap based or direct.
     *
     * @param buffer to which the view is attached.
     */
    void wrap(ByteBuffer buffer);

    /**
     * Attach a view to a {@link ByteBuffer} for providing direct access.
     *
     * @param buffer to which the view is attached.
     * @param offset at which the view begins.
     * @param length of the buffer included in the view
     */
    void wrap(ByteBuffer buffer, long offset, int length);

    /**
     * Attach a view to an existing {@link LongDirectBuffer}
     *
     * @param buffer to which the view is attached.
     */
    void wrap(LongDirectBuffer buffer);

    /**
     * Attach a view to a {@link LongDirectBuffer} for providing direct access.
     *
     * @param buffer to which the view is attached.
     * @param offset at which the view begins.
     * @param length of the buffer included in the view
     */
    void wrap(LongDirectBuffer buffer, long offset, long length);

    /**
     * Attach a view to an off-heap memory region by address.
     *
     * @param address where the memory begins off-heap
     * @param length  of the buffer from the given address
     */
    void wrap(long address, long length);

    /**
     * Reads the underlying offset to to the memory address.
     *
     * @return the underlying offset to to the memory address.
     */
    long addressOffset();

    /**
     * Get the underlying byte[] if one exists.
     *
     * @return the underlying byte[] if one exists.
     */
    byte[] byteArray();

    /**
     * Get the underlying {@link ByteBuffer} if one exists.
     *
     * @return the underlying {@link ByteBuffer} if one exists.
     */
    ByteBuffer byteBuffer();

    /**
     * Get the capacity of the underlying buffer.
     *
     * @return the capacity of the underlying buffer in bytes.
     */
    long capacity();

    /**
     * Check that a given limit is not greater than the capacity of a buffer from a given offset.
     * <p>
     * Can be overridden in a DirectBuffer subclass to enable an extensible buffer or handle retry after a flush.
     *
     * @param limit up to which access is required.
     * @throws IndexOutOfBoundsException if limit is beyond buffer capacity.
     */
    void checkLimit(long limit);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value for at a given index
     */
    long getLong(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value for at a given index
     */
    long getLong(long index);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    int getInt(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value for at a given index
     */
    int getInt(long index);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    double getDouble(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    double getDouble(long index);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    float getFloat(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    float getFloat(long index);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    short getShort(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    short getShort(long index);

    /**
     * Get the value at a given index.
     *
     * @param index     in bytes from which to get.
     * @param byteOrder of the value to be read.
     * @return the value at a given index.
     */
    char getChar(long index, ByteOrder byteOrder);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    char getChar(long index);

    /**
     * Get the value at a given index.
     *
     * @param index in bytes from which to get.
     * @return the value at a given index.
     */
    byte getByte(long index);

    /**
     * Get from the underlying buffer into a supplied byte array.
     * This method will try to fill the supplied byte array.
     *
     * @param index in the underlying buffer to start from.
     * @param dst   into which the dst will be copied.
     */
    void getBytes(long index, byte[] dst);

    /**
     * Get bytes from the underlying buffer into a supplied byte array.
     *
     * @param index  in the underlying buffer to start from.
     * @param dst    into which the bytes will be copied.
     * @param offset in the supplied buffer to start the copy
     * @param length of the supplied buffer to use.
     */
    void getBytes(long index, byte[] dst, long offset, int length);

    /**
     * Get bytes from this {@link LongDirectBuffer} into the provided {@link LongMutableDirectBuffer} at given indices.
     * @param index     in this buffer to begin getting the bytes.
     * @param dstBuffer to which the bytes will be copied.
     * @param dstIndex  in the channel buffer to which the byte copy will begin.
     * @param length    of the bytes to be copied.
     */
    void getBytes(long index, LongMutableDirectBuffer dstBuffer, long dstIndex, int length);

    /**
     * Get from the underlying buffer into a supplied {@link ByteBuffer}.
     *
     * @param index     in the underlying buffer to start from.
     * @param dstBuffer into which the bytes will be copied.
     * @param length    of the supplied buffer to use.
     */
    void getBytes(long index, ByteBuffer dstBuffer, int length);

    /**
     * Get a String from bytes encoded in UTF-8 format that is length prefixed.
     *
     * @param offset    at which the String begins.
     * @param byteOrder for the length at the beginning of the String.
     * @return the String as represented by the UTF-8 encoded bytes.
     */
    String getStringUtf8(long offset, ByteOrder byteOrder);

    /**
     * Get part of String from bytes encoded in UTF-8 format that is length prefixed.
     *
     * @param offset at which the String begins.
     * @param length of the String in bytes to decode.
     * @return the String as represented by the UTF-8 encoded bytes.
     */
    String getStringUtf8(long offset, int length);

    /**
     * Get an encoded UTF-8 String from the buffer that does not have a length prefix.
     *
     * @param offset at which the String begins.
     * @param length of the String in bytes to decode.
     * @return the String as represented by the UTF-8 encoded bytes.
     */
    String getStringWithoutLengthUtf8(long offset, int length);

    /**
     * Check that a given length of bytes is within the bounds from a given index.
     *
     * @param index  from which to check.
     * @param length in bytes of the range to check.
     * @throws java.lang.IndexOutOfBoundsException if the length goes outside of the capacity range.
     */
    void boundsCheck(long index, int length);
}
