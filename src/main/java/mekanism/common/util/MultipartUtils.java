package mekanism.common.util;

import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

public final class MultipartUtils {

    public static AxisAlignedBB rotate(AxisAlignedBB aabb, EnumFacing side) {
        Vec3d v1 = rotate(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), side);
        Vec3d v2 = rotate(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ), side);

        return new AxisAlignedBB(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
    }

    public static Vec3d rotate(Vec3d vec, EnumFacing side) {
        switch (side) {
            case DOWN:
                return new Vec3d(vec.x, vec.y, vec.z);
            case UP:
                return new Vec3d(vec.x, -vec.y, -vec.z);
            case NORTH:
                return new Vec3d(vec.x, -vec.z, vec.y);
            case SOUTH:
                return new Vec3d(vec.x, vec.z, -vec.y);
            case WEST:
                return new Vec3d(vec.y, -vec.x, vec.z);
            case EAST:
                return new Vec3d(-vec.y, vec.x, vec.z);
        }

        return null;
    }

    /* taken from MCMP */
    public static Pair<Vec3d, Vec3d> getRayTraceVectors(EntityPlayer player) {
        float pitch = player.rotationPitch;
        float yaw = player.rotationYaw;
        Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        float f5 = f2 * f3;
        float f6 = f1 * f3;
        double d3 = 5.0D;

        if (player instanceof EntityPlayerMP) {
            d3 = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        }

        Vec3d end = start.add(f5 * d3, f4 * d3, f6 * d3);
        return Pair.of(start, end);
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end,
          Collection<AxisAlignedBB> boxes) {
        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;
        int i = -1;

        for (AxisAlignedBB aabb : boxes) {
            AdvancedRayTraceResult result = aabb == null ? null : collisionRayTrace(pos, start, end, aabb, i, null);

            if (result != null) {
                double d = result.squareDistanceTo(start);

                if (d < minDistance) {
                    minDistance = d;
                    hit = result;
                }
            }

            i++;
        }

        return hit;
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB bounds,
          int subHit, Object hitInfo) {
        RayTraceResult result = bounds.offset(pos).calculateIntercept(start, end);

        if (result == null) {
            return null;
        }

        result = new RayTraceResult(RayTraceResult.Type.BLOCK, result.hitVec, result.sideHit, pos);
        result.subHit = subHit;
        result.hitInfo = hitInfo;

        return new AdvancedRayTraceResult(result, bounds);
    }

    private static class AdvancedRayTraceResultBase<T extends RayTraceResult> {

        public final AxisAlignedBB bounds;
        public final T hit;

        public AdvancedRayTraceResultBase(T mop, AxisAlignedBB aabb) {
            hit = mop;
            bounds = aabb;
        }

        public boolean valid() {
            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vec3d vec) {
            return hit.hitVec.squareDistanceTo(vec);
        }
    }

    public static class AdvancedRayTraceResult extends AdvancedRayTraceResultBase<RayTraceResult> {

        public AdvancedRayTraceResult(RayTraceResult mop, AxisAlignedBB bounds) {
            super(mop, bounds);
        }
    }
}
