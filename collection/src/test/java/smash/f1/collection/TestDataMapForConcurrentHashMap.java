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

import java.util.concurrent.ConcurrentHashMap;

import com.eaio.uuid.UUID;

public final class TestDataMapForConcurrentHashMap implements TestDataMap
{
	private int size;
	private ConcurrentHashMap<TestDataUUID,TestDataForJavaMap> map;
			
	public TestDataMapForConcurrentHashMap( final int aSize )
	{
		size = aSize;
		map = new ConcurrentHashMap<TestDataUUID,TestDataForJavaMap>( aSize, 0.75f );
	}
	
	@Override
	public void put(final TestData aData) 
	{
		TestDataForJavaMap data = (TestDataForJavaMap)aData;
		map.put(data.getKey(),data);
	}

	@Override
	public TestData get(final TestData aData) 
	{
		TestDataForJavaMap data = (TestDataForJavaMap)aData;
		return map.get( data.getKey() );
	}

	@Override
	public boolean remove(final TestData aData) 
	{
		TestDataForJavaMap data = (TestDataForJavaMap)aData;
		data = map.remove( data.getKey() );
		return data != null;
	}

	@Override
	public boolean contains(final TestData aData) 
	{
		TestDataForJavaMap data = (TestDataForJavaMap)aData;
		return map.containsKey(data.getKey());
	}

	@Override
	public long getSize() 
	{
		return map.size();
	}
	
	@Override
	public boolean getZeroCopy(final TestData aData ) 
	{
		throw new RuntimeException( "Not supported" );
	}

	@Override
	public TestData createTestData() 
	{
		return new TestDataForJavaMap( size );
	}
	
	@Override
	public boolean needNewData() 
	{
		return true;
	}

	@Override
	public void clear() 
	{
		map.clear();
	}
	
	@Override
	public TestData createTestDataForZeroCopy()
	{
		return null;
	}
	
	@Override
	public void dispose() 
	{
		
	}
}
