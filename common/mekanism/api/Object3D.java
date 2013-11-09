package mekanism.api;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class Object3D 
{
	public int xCoord;
	public int yCoord;
	public int zCoord;
	
	public int dimensionId;
	
	public Object3D(int x, int y, int z)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		
		dimensionId = 0;
	}
	
	public Object3D(int x, int y, int z, int dimension)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		
		dimensionId = dimension;
	}
	
	public int getMetadata(IBlockAccess world)
	{
		return world.getBlockMetadata(xCoord, yCoord, zCoord);
	}
	
	public int getBlockId(IBlockAccess world)
	{
		return world.getBlockId(xCoord, yCoord, zCoord);
	}
	
	public TileEntity getTileEntity(IBlockAccess world)
	{
		if(!(world instanceof World && ((World)world).blockExists(xCoord, yCoord, zCoord)))
		{
			return null;
		}
		
		return world.getBlockTileEntity(xCoord, yCoord, zCoord);
	}
	
	public NBTTagCompound write(NBTTagCompound nbtTags)
	{
		nbtTags.setInteger("x", xCoord);
		nbtTags.setInteger("y", yCoord);
		nbtTags.setInteger("z", zCoord);
		nbtTags.setInteger("dimensionId", dimensionId);
		
		return nbtTags;
	}
	
	public void write(ArrayList data)
	{
		data.add(xCoord);
		data.add(yCoord);
		data.add(zCoord);
	}
	
	public Object3D translate(int x, int y, int z)
	{
		xCoord += x;
		yCoord += y;
		zCoord += z;
		
		return this;
	}
	
	public Object3D getFromSide(ForgeDirection side)
	{
		return new Object3D(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ, dimensionId);
	}
	
	public static Object3D get(TileEntity tileEntity)
	{
		return new Object3D(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity.worldObj.provider.dimensionId);
	}
	
	public static Object3D read(NBTTagCompound nbtTags)
	{
		return new Object3D(nbtTags.getInteger("x"), nbtTags.getInteger("y"), nbtTags.getInteger("z"), nbtTags.getInteger("dimensionId"));
	}
	
	public static Object3D read(ByteArrayDataInput dataStream)
	{
		return new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
	
	public Object3D difference(Object3D other)
	{
		return new Object3D(xCoord-other.xCoord, yCoord-other.yCoord, zCoord-other.zCoord);
	}
	
	public ForgeDirection sideDifference(Object3D other)
	{
		Object3D diff = difference(other);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(side.offsetX == diff.xCoord && side.offsetY == diff.yCoord && side.offsetZ == diff.zCoord)
			{
				return side;
			}
		}
		
		return ForgeDirection.UNKNOWN;
	}
	
	public int distanceTo(Object3D obj)
	{
	    int subX = xCoord - obj.xCoord;
	    int subY = yCoord - obj.yCoord;
	    int subZ = zCoord - obj.zCoord;
	    return (int)MathHelper.sqrt_double(subX * subX + subY * subY + subZ * subZ);
	}
	
	public boolean sideVisible(ForgeDirection side, IBlockAccess world)
	{
		return world.getBlockId(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ) == 0;
	}
	
	public Object3D step(ForgeDirection side)
	{
		return translate(side.offsetX, side.offsetY, side.offsetZ);
	}
	
	@Override
	public String toString()
	{
		return "[Object3D: " + xCoord + ", " + yCoord + ", " + zCoord + "]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Object3D && 
				((Object3D)obj).xCoord == xCoord && 
				((Object3D)obj).yCoord == yCoord && 
				((Object3D)obj).zCoord == zCoord && 
				((Object3D)obj).dimensionId == dimensionId;
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
}