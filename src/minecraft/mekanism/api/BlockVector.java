package mekanism.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockVector 
{
	public int xCoord;
	public int yCoord;
	public int zCoord;
	public int dimensionId;
	
	public BlockVector(int x, int y, int z)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		dimensionId = 0;
	}
	
	public BlockVector(int x, int y, int z, int dimension)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		dimensionId = dimension;
	}
	
	public TileEntity getTileEntity(World world)
	{
		return world.getBlockTileEntity(xCoord, yCoord, zCoord);
	}
	
	public void write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("x", xCoord);
		nbtTags.setInteger("y", yCoord);
		nbtTags.setInteger("z", zCoord);
		nbtTags.setInteger("dimensionId", dimensionId);
	}
	
	public BlockVector getFromSide(ForgeDirection side)
	{
		return new BlockVector(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ, dimensionId);
	}
	
	public static BlockVector get(TileEntity tileEntity)
	{
		return new BlockVector(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity.worldObj.provider.dimensionId);
	}
	
	public static BlockVector read(NBTTagCompound nbtTags)
	{
		return new BlockVector(nbtTags.getInteger("x"), nbtTags.getInteger("y"), nbtTags.getInteger("z"), nbtTags.getInteger("dimensionId"));
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BlockVector && 
				((BlockVector)obj).xCoord == xCoord && 
				((BlockVector)obj).yCoord == yCoord && 
				((BlockVector)obj).zCoord == zCoord && 
				((BlockVector)obj).dimensionId == dimensionId;
	}
	
	@Override
	public int hashCode() 
	{
		int code = 1;
		code = 31 * code + xCoord;
		code = 31 * code + yCoord;
		code = 31 * code + zCoord;
		return code;
	}
}
