package smash.f1.collection;

import smash.f1.core.agrona.LongDirectBuffer;

public class TestDataForF1BinaryMapToString implements KeyValueToString
{

	@Override
	public String convertKey(LongDirectBuffer aKey, long aKeyStartIndex,
			int aKeyLength) {
		StringBuilder builder = new StringBuilder();
		builder.append( "KEY:" + aKey.getLong(aKeyStartIndex) + ":" + aKey.getLong(aKeyStartIndex+8));
		return builder.toString();
	}

	@Override
	public String convertValue(LongDirectBuffer aValue, long aValueStartIndex,
			int aKeyLength) {
		StringBuilder builder = new StringBuilder();
		builder.append( "VALUE:" + aValue.getLong(aValueStartIndex-16) + ":" + aValue.getLong(aValueStartIndex-8)+ ":" + aValue.getLong(aValueStartIndex)+ ":" + aValue.getLong(aValueStartIndex+8)
				+ ":" + aValue.getLong(aValueStartIndex+16)+ ":" + aValue.getLong(aValueStartIndex+24));
		return builder.toString();
	}

}
