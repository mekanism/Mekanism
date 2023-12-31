package mekanism.common.lib.math;

import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

// can add to this as we see necessary
public record Plane(Vec3 minPos, Vec3 maxPos) {

    public static Plane getInnerCuboidPlane(VoxelCuboid cuboid, CuboidSide side) {
        int minX = cuboid.getMinPos().getX() + 1, minY = cuboid.getMinPos().getY() + 1, minZ = cuboid.getMinPos().getZ() + 1;
        int maxX = cuboid.getMaxPos().getX(), maxY = cuboid.getMaxPos().getY(), maxZ = cuboid.getMaxPos().getZ();
        return switch (side) {
            case NORTH -> new Plane(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, minZ));
            case SOUTH -> new Plane(new Vec3(minX, minY, maxZ), new Vec3(maxX, maxY, maxZ));
            case WEST -> new Plane(new Vec3(minX, minY, minZ), new Vec3(minX, maxY, maxZ));
            case EAST -> new Plane(new Vec3(maxX, minY, minZ), new Vec3(maxX, maxY, maxZ));
            case BOTTOM -> new Plane(new Vec3(minX, minY, minZ), new Vec3(maxX, minY, maxZ));
            case TOP -> new Plane(new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, maxZ));
        };
    }

    public Vec3 getRandomPoint(RandomSource rand) {
        return new Vec3(minPos.x + rand.nextDouble() * (maxPos.x - minPos.x),
              minPos.y + rand.nextDouble() * (maxPos.y - minPos.y),
              minPos.z + rand.nextDouble() * (maxPos.z - minPos.z));
    }
}
