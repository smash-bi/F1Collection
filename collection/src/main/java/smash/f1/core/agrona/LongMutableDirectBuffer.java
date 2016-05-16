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
 * Abstraction over a range of buffer types that allows fields to be written in native typed fashion.
 */
public interface LongMutableDirectBuffer extends LongDirectBuffer
{
    /**
     * Set a region of memory to a given byte value.
     *
     * @param index  at which to start.
     * @param length of the run of bytes to set.
     * @param value  the memory will be set to.
     */
    void setMemory(long index, int length, byte value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     for at a given index
     * @param byteOrder of the value when written
     */
    void putLong(long index, long value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value for at a given index
     */
    void putLong(long index, long value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written
     */
    void putInt(long index, int value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value for at a given index
     */
    void putInt(long index, int value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    void putDouble(long index, double value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    void putDouble(long index, double value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    void putFloat(long index, float value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    void putFloat(long index, float value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    void putShort(long index, short value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    void putShort(long index, short value);

    /**
     * Put a value to a given index.
     *
     * @param index     in bytes for where to put.
     * @param value     to be written
     * @param byteOrder of the value when written.
     */
    void putChar(long index, char value, ByteOrder byteOrder);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    void putChar(long index, char value);

    /**
     * Put a value to a given index.
     *
     * @param index in bytes for where to put.
     * @param value to be written
     */
    void putByte(long index, byte value);

    /**
     * Put an array of src into the underlying buffer.
     *
     * @param index in the underlying buffer to start from.
     * @param src   to be copied to the underlying buffer.
     */
    void putBytes(long index, byte[] src);

    /**
     * Put an array into the underlying buffer.
     *
     * @param index  in the underlying buffer to start from.
     * @param src    to be copied to the underlying buffer.
     * @param offset in the supplied buffer to begin the copy.
     * @param length of the supplied buffer to copy.
     */
    void putBytes(long index, byte[] src, long offset, int length);

    /**
     * Put bytes into the underlying buffer for the view.  Bytes will be copied from current
     * {@link ByteBuffer#position()} to {@link ByteBuffer#limit()}.
     *
     * @param index     in the underlying buffer to start from.
     * @param srcBuffer to copy the bytes from.
     * @param length    of the supplied buffer to copy.
     */
    void putBytes(long index, ByteBuffer srcBuffer, int length);

    /**
     * Put bytes into the underlying buffer for the view. Bytes will be copied from the buffer index to
     * the buffer index + length.
     *
     * @param index     in the underlying buffer to start from.
     * @param srcBuffer to copy the bytes from (does not change position).
     * @param srcIndex  in the source buffer from which the copy will begin.
     * @param length    of the bytes to be copied.
     */
    void putBytes(long index, ByteBuffer srcBuffer, long srcIndex, int length);

    /**
     * Put bytes from a source {@link LongDirectBuffer} into this {@link LongMutableDirectBuffer} at given indices.
     * @param index     in this buffer to begin putting the bytes.
     * @param srcBuffer from which the bytes will be copied.
     * @param srcIndex  in the source buffer from which the byte copy will begin.
     * @param length    of the bytes to be copied.
     */
    void putBytes(long index, LongDirectBuffer srcBuffer, long srcIndex, int length);

    /**
     * Encode a String as UTF-8 bytes to the buffer with a length prefix.
     *
     * @param offset    at which the String should be encoded.
     * @param value     of the String to be encoded.
     * @param byteOrder for the length prefix.
     * @return the number of bytes put to the buffer.
     */
    int putStringUtf8(long offset, String value, ByteOrder byteOrder);

    /**
     * Encode a String as UTF-8 bytes the buffer with a length prefix with a maximum encoded size check.
     *
     * @param offset         at which the String should be encoded.
     * @param value          of the String to be encoded.
     * @param byteOrder      for the length prefix.
     * @param maxEncodedSize to be checked before writing to the buffer.
     * @return the number of bytes put to the buffer.
     * @throws java.lang.IllegalArgumentException if the encoded bytes are greater than maxEncodedSize.
     */
    int putStringUtf8(long offset, String value, ByteOrder byteOrder, int maxEncodedSize);

    /**
     * Encode a String as UTF-8 bytes in the buffer without a length prefix.
     *
     * @param offset at which the String begins.
     * @param value  of the String to be encoded.
     * @return the number of bytes encoded.
     */
    int putStringWithoutLengthUtf8(long offset, String value);
}
