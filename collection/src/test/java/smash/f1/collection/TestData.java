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

/**
 * TestData has the following layout
 * long key1
 * long key2
 * long value1 (value = key1)
 * long value2 (value = key2)
 * long value3 (value = key2)
 * long value4 (value = key1)
 */
public interface TestData 
{
	/**
	 * set key
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setKey( long aKey1, long aKey2 );
	
	/**
	 * set data
	 * @param aKey1 key 1
	 * @param aKey2 key 2
	 */
	public void setData( long aKey1, long aKey2 );
	
	/**
	 * check if the data is correct
	 */
	public boolean isCorrect();
	
	/**
	 * get key 1
	 * @return key 1
	 */
	public long getKey1();
	
	/**
	 * get key 2
	 * @return key 2
	 */
	public long getKey2();
	
	/**
	 * to string
	 */
	public String getPrintableText();
}
