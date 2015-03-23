package mekanism.api;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.FMLCommonHandler;

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
		yMax = 255;
		zMax = zMin+16;
		
		dimensionId = chunk.dimensionId;
	}
	
	public Range4D(Coord4D coord)
	{
		xMin = coord.xCoord;
		yMin = coord.yCoord;
		zMin = coord.zCoord;
		
		xMax = coord.xCoord+1;
		yMax = coord.yCoord+1;
		zMax = coord.zCoord+1;
		
		dimensionId = coord.dimensionId;
	}
	
	public static Range4D getChunkRange(EntityPlayer player)
	{
		int radius = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getViewDistance();
		
		return new Range4D(new Chunk3D(player)).expandChunks(radius);
	}
	
	public Range4D expandChunks(int chunks)
	{
		xMin -= chunks*16;
		xMax += chunks*16;
		zMin -= chunks*16;
		zMax += chunks*16;
		
		return this;
	}
	
	public boolean intersects(Range4D range)
	{
		return (xMax+1 - 1.E-05D > range.xMin) && (range.xMax+1 - 1.E-05D > xMin) && (yMax+1 - 1.E-05D > range.yMin) && (range.yMax+1 - 1.E-05D > yMin) && (zMax+1 - 1.E-05D > range.zMin) && (range.zMax+1 - 1.E-05D > zMin);
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
