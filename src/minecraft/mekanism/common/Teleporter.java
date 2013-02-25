package mekanism.common;

public class Teleporter
{
	/**
	 * Coords -- a set of coordinates as well as a dimension ID that is used by teleporters.
	 * @author aidancbrady
	 *
	 */
	public static final class Coords
	{
		public int xCoord;
		public int yCoord;
		public int zCoord;
		public int dimensionId;
		
		public Coords(int x, int y, int z, int id)
		{
			xCoord = x;
			yCoord = y;
			zCoord = z;
			dimensionId = id;
		}
		
		/**
		 * Gets the Coords from a tile entity.
		 * @param tileEntity
		 * @return coords
		 */
		public static Coords get(TileEntityTeleporter tileEntity)
		{
			return new Coords(tileEntity.xCoord, tileEntity.yCoord+1, tileEntity.zCoord, tileEntity.worldObj.provider.dimensionId);
		}
		
		@Override
		public int hashCode() 
		{
			int code = 1;
			code = 31 * code + xCoord;
			code = 31 * code + yCoord;
			code = 31 * code + zCoord;
			code = 31 * code + dimensionId;
			return code;
		}
		
		@Override
		public boolean equals(Object coords)
		{
			return coords instanceof Coords && ((Coords)coords).xCoord == xCoord && ((Coords)coords).yCoord == yCoord && ((Coords)coords).zCoord == zCoord && ((Coords)coords).dimensionId == dimensionId;
		}
	}
	
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
