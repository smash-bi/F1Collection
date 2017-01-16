package smash.f1.collection;

import smash.f1.core.agrona.LongDirectBuffer;

/**
 * KeyValueVerifier performs verification to verify if the value matches
 * the corresponding key
 */
public interface KeyValueVerifier 
{
	/**
	 * check if the value matches what the key is expecting
	 * @param aKey key
	 * @param aKeyStartIndex start index of the key in the key buffer
	 * @param aValue value buffer
	 * @param aValueStartIndex start index of the value should be copied to
	 */
	public boolean verify( final LongDirectBuffer aKey, final long aKeyStartIndex, final LongDirectBuffer aValue, final long aValueStartIndex );
}
