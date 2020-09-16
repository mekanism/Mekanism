package mekanism.api;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public class Range3D {

    public final RegistryKey<World> dimension;
    public final int xMin;
    public final int zMin;
    public final int xMax;
    public final int zMax;

    public Range3D(int minX, int minZ, int maxX, int maxZ, RegistryKey<World> dimension) {
        xMin = minX;
        zMin = minZ;
        xMax = maxX;
        zMax = maxZ;
        this.dimension = dimension;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Range3D clone() {
        return new Range3D(xMin, zMin, xMax, zMax, dimension);
    }

    @Override
    public String toString() {
        return "[Range3D: " + xMin + ", " + zMin + ", " + xMax + ", " + zMax + ", dim=" + dimension.getLocation() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Range3D &&
               ((Range3D) obj).xMin == xMin &&
               ((Range3D) obj).zMin == zMin &&
               ((Range3D) obj).xMax == xMax &&
               ((Range3D) obj).zMax == zMax &&
               ((Range3D) obj).dimension == dimension;
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + xMin;
        code = 31 * code + zMin;
        code = 31 * code + xMax;
        code = 31 * code + zMax;
        code = 31 * code + dimension.hashCode();
        return code;
    }
}