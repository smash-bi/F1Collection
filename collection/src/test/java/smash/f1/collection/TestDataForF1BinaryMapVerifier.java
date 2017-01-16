package smash.f1.collection;

import smash.f1.core.agrona.LongDirectBuffer;

public class TestDataForF1BinaryMapVerifier implements KeyValueVerifier
{

	@Override
	public boolean verify(LongDirectBuffer aKey, long aKeyStartIndex,
			LongDirectBuffer aValue, long aValueStartIndex) 
	{
		return aKey.getLong(aKeyStartIndex+0) == aValue.getLong(aValueStartIndex-16)
				&&
				aKey.getLong(aKeyStartIndex+8) == aValue.getLong(aValueStartIndex-8)
				&&
				( aValue.getLong(aValueStartIndex-16) == aValue.getLong(aValueStartIndex) && aValue.getLong(aValueStartIndex-16) == aValue.getLong(aValueStartIndex+24) ) 
				&&
				( aValue.getLong(aValueStartIndex-8) == aValue.getLong(aValueStartIndex+8) && aValue.getLong(aValueStartIndex-8) == aValue.getLong(aValueStartIndex+16) );
	}

}
