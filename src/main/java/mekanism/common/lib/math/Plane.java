package mekanism.common.lib.math;

import java.util.Random;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidSide;
import net.minecraft.util.math.vector.Vector3d;

// can add to this as we see necessary
public class Plane {

    private final Vector3d minPos;
    private final Vector3d maxPos;

    public Plane(Vector3d minPos, Vector3d maxPos) {
        this.minPos = minPos;
        this.maxPos = maxPos;
    }

    public static Plane getInnerCuboidPlane(VoxelCuboid cuboid, CuboidSide side) {
        int minX = cuboid.getMinPos().getX() + 1, minY = cuboid.getMinPos().getY() + 1, minZ = cuboid.getMinPos().getZ() + 1;
        int maxX = cuboid.getMaxPos().getX(), maxY = cuboid.getMaxPos().getY(), maxZ = cuboid.getMaxPos().getZ();
        switch (side) {
            case NORTH:
                return new Plane(new Vector3d(minX, minY, minZ), new Vector3d(maxX, maxY, minZ));
            case SOUTH:
                return new Plane(new Vector3d(minX, minY, maxZ), new Vector3d(maxX, maxY, maxZ));
            case WEST:
                return new Plane(new Vector3d(minX, minY, minZ), new Vector3d(minX, maxY, maxZ));
            case EAST:
                return new Plane(new Vector3d(maxX, minY, minZ), new Vector3d(maxX, maxY, maxZ));
            case BOTTOM:
                return new Plane(new Vector3d(minX, minY, minZ), new Vector3d(maxX, minY, maxZ));
            case TOP:
                return new Plane(new Vector3d(minX, maxY, minZ), new Vector3d(maxX, maxY, maxZ));
        }
        return null;
    }

    public Vector3d getRandomPoint(Random rand) {
        return new Vector3d(minPos.x + rand.nextDouble() * (maxPos.x - minPos.x),
              minPos.y + rand.nextDouble() * (maxPos.y - minPos.y),
              minPos.z + rand.nextDouble() * (maxPos.z - minPos.z));
    }
}
