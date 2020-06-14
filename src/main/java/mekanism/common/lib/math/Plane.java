package mekanism.common.lib.math;

import java.util.Random;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import net.minecraft.util.math.Vec3d;

// can add to this as we see necessary
public class Plane {

    private final Vec3d minPos;
    private final Vec3d maxPos;

    public Plane(Vec3d minPos, Vec3d maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public static Plane getInnerCuboidPlane(VoxelCuboid cuboid, CuboidSide side) {
        int minX = cuboid.getMinPos().getX() + 1, minY = cuboid.getMinPos().getY() + 1, minZ = cuboid.getMinPos().getZ() + 1;
        int maxX = cuboid.getMaxPos().getX(), maxY = cuboid.getMaxPos().getY(), maxZ = cuboid.getMaxPos().getZ();
        switch (side) {
            case NORTH:
                return new Plane(new Vec3d(minX, minY, minZ), new Vec3d(maxX, maxY, minZ));
            case SOUTH:
                return new Plane(new Vec3d(minX, minY, maxZ), new Vec3d(maxX, maxY, maxZ));
            case WEST:
                return new Plane(new Vec3d(minX, minY, minZ), new Vec3d(minX, maxY, maxZ));
            case EAST:
                return new Plane(new Vec3d(maxX, minY, minZ), new Vec3d(maxX, maxY, maxZ));
            case BOTTOM:
                return new Plane(new Vec3d(minX, minY, minZ), new Vec3d(maxX, minY, maxZ));
            case TOP:
                return new Plane(new Vec3d(minX, maxY, minZ), new Vec3d(maxX, maxY, maxZ));
        }
        return null;
    }

    public Vec3d getRandomPoint(Random rand) {
        return new Vec3d(minPos.x + rand.nextDouble() * (maxPos.x - minPos.x),
              minPos.y + rand.nextDouble() * (maxPos.y - minPos.y),
              minPos.z + rand.nextDouble() * (maxPos.z - minPos.z));
    }
}
