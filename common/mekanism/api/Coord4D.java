package mekanism.api;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

public class Coord4D 
{
	public int xCoord;
	public int yCoord;
	public int zCoord;
	
	public int dimensionId;
	
	public Coord4D(int x, int y, int z)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		
		dimensionId = 0;
	}
	
	public Coord4D(int x, int y, int z, int dimension)
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
	
	public Coord4D translate(int x, int y, int z)
	{
		xCoord += x;
		yCoord += y;
		zCoord += z;
		
		return this;
	}
	
	public Coord4D getFromSide(ForgeDirection side)
	{
		return new Coord4D(xCoord+side.offsetX, yCoord+side.offsetY, zCoord+side.offsetZ, dimensionId);
	}
	
	public static Coord4D get(TileEntity tileEntity)
	{
		return new Coord4D(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, tileEntity.worldObj.provider.dimensionId);
	}
	
	public static Coord4D read(NBTTagCompound nbtTags)
	{
		return new Coord4D(nbtTags.getInteger("x"), nbtTags.getInteger("y"), nbtTags.getInteger("z"), nbtTags.getInteger("dimensionId"));
	}
	
	public static Coord4D read(ByteArrayDataInput dataStream)
	{
		return new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
	}
	
	public Coord4D difference(Coord4D other)
	{
		return new Coord4D(xCoord-other.xCoord, yCoord-other.yCoord, zCoord-other.zCoord);
	}
	
	public ForgeDirection sideDifference(Coord4D other)
	{
		Coord4D diff = difference(other);
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(side.offsetX == diff.xCoord && side.offsetY == diff.yCoord && side.offsetZ == diff.zCoord)
			{
				return side;
			}
		}
		
		return ForgeDirection.UNKNOWN;
	}
	
	public int distanceTo(Coord4D obj)
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
	
	public Coord4D step(ForgeDirection side)
	{
		return translate(side.offsetX, side.offsetY, side.offsetZ);
	}
	
	public boolean exists(World world)
	{
		return world.getChunkProvider().chunkExists(xCoord >> 4, zCoord >> 4);
	}
	
	public Chunk getChunk(World world)
	{
		return world.getChunkFromBlockCoords(xCoord >> 4, zCoord >> 4);
	}
	
	@Override
	public Coord4D clone()
	{
		return new Coord4D(xCoord, yCoord, zCoord, dimensionId);
	}
	
	@Override
	public String toString()
	{
		return "[Object3D: " + xCoord + ", " + yCoord + ", " + zCoord + "]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Coord4D && 
				((Coord4D)obj).xCoord == xCoord && 
				((Coord4D)obj).yCoord == yCoord && 
				((Coord4D)obj).zCoord == zCoord && 
				((Coord4D)obj).dimensionId == dimensionId;
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