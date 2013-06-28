package mekanism.common;

public class Teleporter
{
	/**
	 * Code -- a way for teleporters to manage frequencies.
	 * @author aidancbrady
	 *
	 */
	public static final class Code
	{
		public int digitOne;
		public int digitTwo;
		public int digitThree;
		public int digitFour;
		
		public Code(int one, int two, int three, int four)
		{
			digitOne = one;
			digitTwo = two;
			digitThree = three;
			digitFour = four;
		}

		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + digitOne;
			code = 31 * code + digitTwo;
			code = 31 * code + digitThree;
			code = 31 * code + digitFour;
			return code;
		}
		
		@Override
		public boolean equals(Object code)
		{
			return code instanceof Code && ((Code)code).digitOne == digitOne && ((Code)code).digitTwo == digitTwo && ((Code)code).digitThree == digitThree && ((Code)code).digitFour == digitFour;
		}
	}
}
