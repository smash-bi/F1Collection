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
 * F1BinaryMapStatistics provides statistics information for the F1 binary map
 */
public final class F1BinaryMapStatistics 
{
	private long noOfBucketsUsed;
	private long maxChainLength;
	private long noOfBucketsWithMaxChainLength;
	private long noOfExpansions;
	private long noOfEvictions;
	private long freeList;
	private long size;
	
	/**
	 * set statistical information
	 * @param aNoOfBucketsUsed no of buckets current used
	 * @param aMaxChainLength max length of all the bucket chains 
	 * @param aNoOfBucketsWithMaxChainLength get no of buckets with maximum chain length
	 * @param aNoOfExpansions no of expansions
	 * @param aNoOfEvictions no of evictions 
	 * @param aNoOfFreeLists no of free lists
	 * @param aSize size of the map at the time statistics is gathered
	 */
	public void setStatisticalInfo( final long aNoOfBucketsUsed, final long aMaxChainLength, final long aNoOfBucketsWithMaxChainLength,
									final long aNoOfExpansions, final long aNoOfEvictions, final long aNoOfFreeLists, final long aSize )
	{
		noOfBucketsUsed = aNoOfBucketsUsed;
		maxChainLength = aMaxChainLength;
		noOfBucketsWithMaxChainLength = aNoOfBucketsWithMaxChainLength;
		noOfExpansions = aNoOfExpansions;
		noOfEvictions = aNoOfEvictions;	
		freeList = aNoOfFreeLists;
		size = aSize;
	}
	
	/**
	 * get no of buckets are being used 
	 * @return no of buckets in the map are being used
	 */
	public long getNoOfBucketsUsed() 
	{
		return noOfBucketsUsed;
	}
	
	/**
	 * get max length of all the bucket chains 
	 * @return max length of all the bucket chains
	 */
	public long getMaxChainLength() 
	{
		return maxChainLength;
	}
	
	/**
	 * get no of buckets with maximum chain length
	 * @return get no of buckets with maximum chain length
	 */
	public long getNoOfBucketsWithMaxChainLength() 
	{
		return noOfBucketsWithMaxChainLength;
	}
	
	/**
	 * get no of expansions 
	 * @return no of expansions
	 */
	public long getNoOfExpansions() 
	{
		return noOfExpansions;
	}
	
	/**
	 * get no of evictions 
	 * @return no of evictions
	 */
	public long getNoOfEvictions() 
	{
		return noOfEvictions;
	}	
	
	/**
	 * get no of free lists
	 * @return no of free lists
	 */
	public long getNoOfFreeLists()
	{
		return freeList;
	}
	
	/**
	 * get size
	 * @return size
	 */
	public long	getSize()
	{
		return size;
	}
}
