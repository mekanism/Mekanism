package mekanism.api;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Range4D {

    public int dimensionId;

    public int xMin;
    public int yMin;
    public int zMin;
    public int xMax;
    public int yMax;
    public int zMax;

    public Range4D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int dimension) {
        xMin = minX;
        yMin = minY;
        zMin = minZ;
        xMax = maxX;
        yMax = maxY;
        zMax = maxZ;

        dimensionId = dimension;
    }

    public Range4D(Chunk3D chunk) {
        xMin = chunk.x * 16;
        yMin = 0;
        zMin = chunk.z * 16;
        xMax = xMin + 16;
        yMax = 255;
        zMax = zMin + 16;

        dimensionId = chunk.dimensionId;
    }

    public Range4D(Coord4D coord) {
        xMin = coord.x;
        yMin = coord.y;
        zMin = coord.z;

        xMax = coord.x + 1;
        yMax = coord.y + 1;
        zMax = coord.z + 1;

        dimensionId = coord.dimensionId;
    }

    public static Range4D getChunkRange(EntityPlayer player) {
        int radius = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getViewDistance();

        return new Range4D(new Chunk3D(player)).expandChunks(radius);
    }

    public Range4D expandChunks(int chunks) {
        xMin -= chunks * 16;
        xMax += chunks * 16;
        zMin -= chunks * 16;
        zMax += chunks * 16;

        return this;
    }

    public Range4D expandFromCenter(int radius) {
        xMin -= radius;
        xMax += radius;
        zMin -= radius;
        zMax += radius;

        return this;
    }

    public Set<Chunk3D> getIntersectingChunks() {
        Set<Chunk3D> set = new HashSet<>();

        for (int chunkX = xMin >> 4; chunkX <= xMax - 1 >> 4; chunkX++) {
            for (int chunkZ = zMin >> 4; chunkZ <= zMax - 1 >> 4; chunkZ++) {
                set.add(new Chunk3D(chunkX, chunkZ, dimensionId));
            }
        }

        return set;
    }

    public boolean intersects(Range4D range) {
        return (xMax + 1 - 1.E-05D > range.xMin) && (range.xMax + 1 - 1.E-05D > xMin) && (yMax + 1 - 1.E-05D
              > range.yMin) && (range.yMax + 1 - 1.E-05D > yMin) && (zMax + 1 - 1.E-05D > range.zMin) && (
              range.zMax + 1 - 1.E-05D > zMin);
    }

    @Override
    public Range4D clone() {
        return new Range4D(xMin, yMin, zMin, xMax, yMax, zMax, dimensionId);
    }

    @Override
    public String toString() {
        return "[Range4D: " + xMin + ", " + yMin + ", " + zMin + ", " + xMax + ", " + yMax + ", " + zMax + ", dim="
              + dimensionId + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Range4D &&
              ((Range4D) obj).xMin == xMin &&
              ((Range4D) obj).yMin == yMin &&
              ((Range4D) obj).zMin == zMin &&
              ((Range4D) obj).xMax == xMax &&
              ((Range4D) obj).yMax == yMax &&
              ((Range4D) obj).zMax == zMax &&
              ((Range4D) obj).dimensionId == dimensionId;
    }

    @Override
    public int hashCode() {
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
