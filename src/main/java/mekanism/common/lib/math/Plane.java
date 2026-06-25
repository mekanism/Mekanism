package mekanism.common.lib.math;

import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import org.joml.Vector3fc;

// can add to this as we see necessary
public record Plane(Vector3fc minPos, Vector3fc maxPos) {

    public static Plane getInnerCuboidPlane(VoxelCuboid cuboid, CuboidSide side) {
        int minX = cuboid.getMinPos().getX() + 1, minY = cuboid.getMinPos().getY() + 1, minZ = cuboid.getMinPos().getZ() + 1;
        int maxX = cuboid.getMaxPos().getX(), maxY = cuboid.getMaxPos().getY(), maxZ = cuboid.getMaxPos().getZ();
        return switch (side) {
            case NORTH -> new Plane(new Vector3f(minX, minY, minZ), new Vector3f(maxX, maxY, minZ));
            case SOUTH -> new Plane(new Vector3f(minX, minY, maxZ), new Vector3f(maxX, maxY, maxZ));
            case WEST -> new Plane(new Vector3f(minX, minY, minZ), new Vector3f(minX, maxY, maxZ));
            case EAST -> new Plane(new Vector3f(maxX, minY, minZ), new Vector3f(maxX, maxY, maxZ));
            case BOTTOM -> new Plane(new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, maxZ));
            case TOP -> new Plane(new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, maxZ));
        };
    }

    public Vector3f getRandomPoint(RandomSource rand) {
        return new Vector3f(minPos.x() + rand.nextFloat() * (maxPos.x() - minPos.x()),
              minPos.y() + rand.nextFloat() * (maxPos.y() - minPos.y()),
              minPos.z() + rand.nextFloat() * (maxPos.z() - minPos.z()));
    }
}
