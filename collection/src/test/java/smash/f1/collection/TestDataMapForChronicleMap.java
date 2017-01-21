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

import java.io.IOException;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.map.ChronicleMap;

public final class TestDataMapForChronicleMap implements TestDataMap
{
	private ChronicleMap<TestDataKeyForChronicleMap,TestDataForChronicleMap> map;
	private TestDataKeyForChronicleMap key = new TestDataKeyForChronicleMap();
	
	public TestDataMapForChronicleMap( long aMaxNoOfRecords ) throws IOException
    {
		map = ChronicleMap.of( TestDataKeyForChronicleMap.class, TestDataForChronicleMap.class)
				.name( "TestDataMapForChronicleMap")
				.entries(aMaxNoOfRecords)
				.putReturnsNull( true )
				.create();
		BytesStore bytesStore = BytesStore.wrap( new byte[16] );
		key.bytesStore( bytesStore, 0, bytesStore.capacity() );
    }
	
	/**
	 * put test data into the map
	 * @param aData
	 */
	public void put( final TestData aData )
	{
		key.setKey( aData.getKey1(), aData.getKey2() );
		map.put( key, (TestDataForChronicleMap)aData );
	}
	
	/**
	 * get test data from the map
	 */
	public TestData get( final TestData aData )
	{
		key.setKey( aData.getKey1(), aData.getKey2() );
		return map.getUsing( key, (TestDataForChronicleMap)aData );	
	}
	
	/**
	 * remove the test data from the map
	 */
	public boolean remove( final TestData aData )
	{
		key.setKey( aData.getKey1(), aData.getKey2() );
		return map.remove( key ) != null;
	}
	
	/**
	 * get if the map contains the test data
	 */
	public boolean contains( final TestData aData )
	{
		key.setKey( aData.getKey1(), aData.getKey2() );
		return map.containsKey( key );
	}
	
	/**
	 * dispose the map and releases all the resources
	 * @param shouldRemoveFiles true will remove all the existing memory mapped files from the system
	 */
	public void dispose( final boolean shouldRemoveFiles )
	{
		map.close();
	}
	
	/**
	 * get size
	 * @return size of the map
	 */
	public long getSize()
	{
		return map.size();
	}
	
	/**
	 * clear all the values from the map
	 */
	public void clear()
	{
		map.clear();
	}

	@Override
	public boolean getZeroCopy(final TestData aData ) 
	{
		key.setKey( aData.getKey1(), aData.getKey2() );
		return map.getUsing( key, (TestDataForChronicleMap)aData ) != null;	
	}

	@Override
	public TestData createTestData() 
	{
		TestDataForChronicleMap data = new TestDataForChronicleMap();
		BytesStore bytesStore = BytesStore.wrap( new byte[48] );
		data.bytesStore( bytesStore, 0, bytesStore.capacity() );
		return data;
	}
	
	@Override
	public TestData createTestDataForZeroCopy()
	{
		//return createTestData();
		return null;
	}

	@Override
	public boolean needNewData() 
	{
		return false;
	}

	@Override
	public void dispose() 
	{
		map.close();
	}
	
	/**
	 * get heap memory
	 */
	public long getOffHeapMemory()
	{
		return map.offHeapMemoryUsed();
	}
	
	@Override
	public boolean isZeroCopyGetAllowed()
	{
		return false;
	}
}
