package mekanism.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class BlockWrapper 
{
	public int x;
	public int y;
	public int z;
	
	public BlockWrapper(int i, int j, int k)
	{
		x = i;
		y = j;
		z = k;
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("x", x);
		nbtTags.setInteger("y", y);
		nbtTags.setInteger("z", z);
	}
	
	public static BlockWrapper get(TileEntity tileEntity)
	{
		return new BlockWrapper(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
	}
	
	public static BlockWrapper read(NBTTagCompound nbtTags)
	{
		return new BlockWrapper(nbtTags.getInteger("x"), nbtTags.getInteger("y"), nbtTags.getInteger("z"));
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BlockWrapper && ((BlockWrapper)obj).x == x && ((BlockWrapper)obj).y == y && ((BlockWrapper)obj).z == z;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + x;
		code = 31 * code + y;
		code = 31 * code + z;
		return code;
	}
}
