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

public interface TestDataMap 
{
	/**
	 * create test data
	 */
	public TestData createTestData();
	
	/**
	 * create test data for zero copy
	 */
	public TestData createTestDataForZeroCopy();
	
	/**
	 * check if map requires new data in each entry
	 * @return true if new data is needed for each entry or false if data can be reused
	 */
	public boolean needNewData();
	
	/**
	 * put test data into the map
	 * @param aData
	 */
	public void put( TestData aData );
	
	/**
	 * get test data from the map
	 */
	public TestData get( TestData aData );
	
	/**
	 * get test data from the map with zero copy
	 */
	public boolean getZeroCopy( TestData aData );
	
	/**
	 * remove the test data from the map
	 */
	public boolean remove( TestData aData );
	
	/**
	 * get if the map contains the test data
	 */
	public boolean contains( TestData aData );
	
	/**
	 * get size
	 */
	public long getSize();
	
	/**
	 * clear the map
	 */
	public void clear();
	
	/**
	 * dispose
	 */
	public void dispose();
}
