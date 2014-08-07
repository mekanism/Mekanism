package mekanism.api;

import net.minecraft.server.MinecraftServer;

public class Range4D 
{
	public int dimensionId;
	
	public int xMin;
	public int yMin;
	public int zMin;
	public int xMax;
	public int yMax;
	public int zMax;
	
	public Range4D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int dimension)
	{
		xMin = minX;
		yMin = minY;
		zMin = minZ;
		xMax = maxX;
		yMax = maxY;
		zMax = maxZ;
		
		dimensionId = dimension;
	}
	
	public Range4D(Chunk3D chunk)
	{
		xMin = chunk.xCoord*16;
		yMin = 0;
		zMin = chunk.zCoord*16;
		xMax = xMin+16;
	}
	
	public static Range4D getLoadedChunks(MinecraftServer server)
	{
		int range = server.getConfigurationManager().getViewDistance();
		
		
		
		return null;
	}
	
	@Override
	public Range4D clone()
	{
		return new Range4D(xMin, yMin, zMin, xMax, yMax, zMax, dimensionId);
	}

	@Override
	public String toString()
	{
		return "[Range4D: " + xMin + ", " + yMin + ", " + zMin + ", " + xMax + ", " + yMax + ", " + zMax + ", dim=" + dimensionId + "]";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Range4D &&
				((Range4D)obj).xMin == xMin &&
				((Range4D)obj).yMin == yMin &&
				((Range4D)obj).zMin == zMin &&
				((Range4D)obj).xMax == xMax &&
				((Range4D)obj).yMax == yMax &&
				((Range4D)obj).zMax == zMax &&
				((Range4D)obj).dimensionId == dimensionId;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + xMin;
		code = 31 * code + yMin;
		code = 31 * code + zMin;
		code = 31 * code + xMax;
		code = 31 * code + yMax;
		code = 31 * code + zMax;
		code = 31 * code + dimensionId;
		return code;
	}
}
