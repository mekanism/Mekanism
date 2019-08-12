package mekanism.api;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class Range4D {

    public DimensionType dimension;
    public int xMin;
    public int yMin;
    public int zMin;
    public int xMax;
    public int yMax;
    public int zMax;

    public Range4D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, DimensionType dimension) {
        xMin = minX;
        yMin = minY;
        zMin = minZ;
        xMax = maxX;
        yMax = maxY;
        zMax = maxZ;
        this.dimension = dimension;
    }

    public Range4D(Chunk3D chunk) {
        xMin = chunk.x * 16;
        yMin = 0;
        zMin = chunk.z * 16;
        xMax = xMin + 16;
        yMax = 255;
        zMax = zMin + 16;
        dimension = chunk.dimension;
    }

    public Range4D(Coord4D coord) {
        this(coord.x, coord.y, coord.z, coord.x + 1, coord.y + 1, coord.z + 1, coord.dimension);
    }

    public static Range4D getChunkRange(PlayerEntity player) {
        int radius = ServerLifecycleHooks.getCurrentServer().getPlayerList().getViewDistance();
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
                set.add(new Chunk3D(chunkX, chunkZ, dimension));
            }
        }
        return set;
    }

    public boolean intersects(Range4D range) {
        return (xMax + 0.99999 > range.xMin) && (range.xMax + 0.99999 > xMin) && (yMax + 0.99999 > range.yMin) &&
               (range.yMax + 0.99999 > yMin) && (zMax + 0.99999 > range.zMin) && (range.zMax + 0.99999 > zMin);
    }

    public boolean hasPlayerInRange(ServerPlayerEntity player) {
        if (player.dimension != dimension) {
            return false;
        }
        //Ignore height for partial Cubic chunks support as range comparision gets used ignoring player height normally anyways
        int radius = player.server.getPlayerList().getViewDistance() * 16;
        int playerX = (int) player.posX;
        int playerZ = (int) player.posZ;
        //playerX/Z + radius is the max, so to stay in line with how it was before,
        // it has an extra + 1 added to it
        return (playerX + radius + 1.99999 > xMin) && (xMax + 0.99999 > playerX - radius) &&
               (playerZ + radius + 1.99999 > zMin) && (zMax + 0.99999 > playerZ - radius);
    }

    @Override
    public Range4D clone() {
        return new Range4D(xMin, yMin, zMin, xMax, yMax, zMax, dimension);
    }

    @Override
    public String toString() {
        return "[Range4D: " + xMin + ", " + yMin + ", " + zMin + ", " + xMax + ", " + yMax + ", " + zMax + ", dim=" + dimension + "]";
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
               ((Range4D) obj).dimension == dimension;
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
        code = 31 * code + dimension.hashCode();
        return code;
    }
}