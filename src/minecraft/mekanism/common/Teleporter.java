package mekanism.common;

public class Teleporter
{
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
		
		public static Coords get(TileEntityTeleporter tileEntity)
		{
			return new Coords(tileEntity.xCoord, tileEntity.yCoord+1, tileEntity.zCoord, tileEntity.worldObj.provider.dimensionId);
		}
		
		@Override
		public boolean equals(Object coords)
		{
			return coords instanceof Coords && ((Coords)coords).xCoord == xCoord && ((Coords)coords).yCoord == yCoord && ((Coords)coords).zCoord == zCoord && ((Coords)coords).dimensionId == dimensionId;
		}
	}
	
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
			return Integer.parseInt(1 + "" + digitOne + "" + digitTwo + "" + digitThree + "" + digitFour);
		}
		
		@Override
		public boolean equals(Object code)
		{
			return code instanceof Code && ((Code)code).digitOne == digitOne && ((Code)code).digitTwo == digitTwo && ((Code)code).digitThree == digitThree && ((Code)code).digitFour == digitFour;
		}
	}
}
