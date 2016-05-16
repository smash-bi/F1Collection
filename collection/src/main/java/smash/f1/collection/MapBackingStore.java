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

/**
 * MapBackingStore is the backing store for the F1BinaryMap using various memory media to support the map memory regions
 */
public interface MapBackingStore 
{
	/**
     * get long value of the given address from the header memory region
     * @param anAddress address of the memory
     * @return long value at the memory region
     */
    public long getLongFromHeaderMemoryRegion( final long anAddress );
    
	/**
     * get long value in volatile memory manner of the given address from the header memory region
     * @param anAddress address of the memory
     * @return long value at the memory region
     */
    public long getLongVolatileFromHeaderMemoryRegion( final long anAddress );
    
    /** 
     * put long value at the given header address in the memory region
     * @param anAddress address of the memory
     * @param aValue value to be placed at the memory region
     */
    public void putLongInHeaderMemoryRegion( final long anAddress, final long aNewValue );
    
    /** 
     * put long value at the given address in the header memory region when the current value in the memory region is
     * the equal to the given old value 
     * @param anAddress address of the memory
     * @param aNewValue value to be placed at the memory region
     * @param anOldValue old value to be compared with the existing value in the memory region and new value will be updated 
     * only when the existing value is equal to this old value
     * @return true if the value has been set or false if the value does not match with the existing value and new value has not been set
     */
    public boolean compareAndPutLongInHeaderMemoryRegion( final long anAddress, final long aNewValue, final long anOldValue );
    
    /**
     * get long value of the given address from the memory region
     * @param anAddress address of the memory
     * @return long value at the memory region
     */
    public long getLongFromMemoryRegion( final long anAddress );
    
    /** 
     * put long value at the given address in the memory region
     * @param anAddress address of the memory
     * @param aValue value to be placed at the memory region
     */
    public void putLongInMemoryRegion( final long anAddress, final long aNewValue );
    
    /** 
     * put long value at the given address in the memory region when the current value in the memory region is
     * the equal to the given old value 
     * @param anAddress address of the memory
     * @param aNewValue value to be placed at the memory region
     * @param anOldValue old value to be compared with the existing value in the memory region and new value will be updated 
     * only when the existing value is equal to this old value
     */
    public void putLongInMemoryRegion( final long anAddress, final long aNewValue, final long anOldValue );
    
    /**
     * get bytes of the given address from the memory region into given buffer
     * @param anAddress starting address of the memory
     * @param aLength length of the data 
     * @param aReceivingBuffer buffer to be written the data into
     * @param aReceivingBufferStartIndex start index for the receiving buffer
     */
    public void getBytesFromMemoryRegion( final long anAddress, final int aLength, final LongMutableDirectBuffer aReceivingBuffer, final long aReceivingBufferStartIndex );
    
    /**
     * put bytes of the buffer's bytes into the given address of the memory region
     * @param anAddress starting address of the memory
     * @param aLength length of the data 
     * @param aSourceBuffer buffer to be sourced the data from
     * @param aSourceBufferStartIndex source buffer start index
     */
    public void putBytesToMemoryRegion( final long anAddress, final int aLength, final LongDirectBuffer aSourceBuffer, final long aSourceBufferStartIndex );
    
    /**
     * expand the map backing store if possible. Implementation is responsible for updating the new no of memory pages
     * @return new size of the map backing store in bytes if the backing store has been expanded or -1 if the map backing store cannot be expanded anymore
     */
    public long expand( final long aMapHeaderFieldAddressNoOfMemoryPages );
    
    /**
     * get memory region based on the given address
     * @param anAddress address of the memory 
     * @param aSize size of data to access
     * @return memory region of the given address
     */
    public LongAtomicBuffer getMemoryRegion( final long anAddress );
    
    /**
     * get memory buffer address
     * @param anAddress address of the memory
     * @return memory buffer address of the given address
     */
    public long getMemoryBufferAddress( final long anAddress );
    
    /**
     * calculate no of memory pages based on the current map size
     * @param aMapSize map size
     * @return no of memory pages
     */
    public long calculateNoOfMemoryPages( final long aMapSize );
    
	/**
	 * dispose the map and releases all the resources
	 * @param shouldEraseAllPersistedMemory true will remove all the existing persisted memory from the system
	 */
	public void dispose( boolean shouldEraseAllPersistedMemory );
} // MapBackingStore
