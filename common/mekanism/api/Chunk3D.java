package mekanism.api;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class Chunk3D
{
	public int dimensionId;
	
	public int xCoord;
	public int zCoord;
	
	public Chunk3D(int x, int z, int dimension)
	{
		xCoord = x;
		zCoord = z;
		
		dimensionId = dimension;
	}
	
	public Chunk3D(Entity entity)
	{
		xCoord = ((int)entity.posX) >> 4;
		zCoord = ((int)entity.posZ) >> 4;
		
		dimensionId = entity.dimension;
	}
	
	public Chunk3D(Coord4D coord)
	{
		xCoord = coord.xCoord >> 4;
		zCoord = coord.zCoord >> 4;
		
		dimensionId = coord.dimensionId;
	}
	
	public boolean exists(World world)
	{
		return world.getChunkProvider().chunkExists(xCoord, zCoord);
	}
	
	public Chunk getChunk(World world)
	{
		return world.getChunkFromChunkCoords(xCoord, zCoord);
	}
	
	@Override
	public Coord4D clone()
	{
		return new Coord4D(xCoord, zCoord, dimensionId);
	}

	@Override
	public String toString()
	{
		return "[Chunk3D: " + xCoord + ", " + zCoord + ", dim=" + dimensionId + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Coord4D &&
				((Coord4D)obj).xCoord == xCoord &&
				((Coord4D)obj).zCoord == zCoord &&
				((Coord4D)obj).dimensionId == dimensionId;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + xCoord;
		code = 31 * code + zCoord;
		code = 31 * code + dimensionId;
		return code;
	}
}
