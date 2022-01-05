package mekanism.common.lib.math;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record Range3D(int xMin, int zMin, int xMax, int zMax, ResourceKey<Level> dimension) {

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Range3D clone() {
        return new Range3D(xMin, zMin, xMax, zMax, dimension);
    }

    @Override
    public String toString() {
        return "[Range3D: " + xMin + ", " + zMin + ", " + xMax + ", " + zMax + ", dim=" + dimension.location() + "]";
    }
}