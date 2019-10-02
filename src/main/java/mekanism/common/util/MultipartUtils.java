package mekanism.common.util;

import java.util.Collection;
import java.util.Collections;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

public final class MultipartUtils {

    public static AxisAlignedBB rotate(AxisAlignedBB aabb, Direction side) {
        Vec3d v1 = rotate(new Vec3d(aabb.minX, aabb.minY, aabb.minZ), side);
        Vec3d v2 = rotate(new Vec3d(aabb.maxX, aabb.maxY, aabb.maxZ), side);
        return new AxisAlignedBB(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
    }

    public static Vec3d rotate(Vec3d vec, Direction side) {
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
    public static Pair<Vec3d, Vec3d> getRayTraceVectors(Entity entity) {
        float pitch = entity.rotationPitch;
        float yaw = entity.rotationYaw;
        Vec3d start = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        float f5 = f2 * f3;
        float f6 = f1 * f3;
        double d3 = 5.0D;
        if (entity instanceof ServerPlayerEntity) {
            d3 = ((ServerPlayerEntity) entity).getAttribute(PlayerEntity.REACH_DISTANCE).getValue();
        }
        Vec3d end = start.add(f5 * d3, f4 * d3, f6 * d3);
        return Pair.of(start, end);
    }


    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, Collection<AxisAlignedBB> boxes) {
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

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3d start, Vec3d end, AxisAlignedBB bounds, int subHit, Object hitInfo) {
        BlockRayTraceResult result = AxisAlignedBB.rayTrace(Collections.singleton(bounds), start, end, pos);
        if (result == null) {
            return null;
        }
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
            return hit.getHitVec().squareDistanceTo(vec);
        }
    }

    public static class AdvancedRayTraceResult extends AdvancedRayTraceResultBase<RayTraceResult> {

        public AdvancedRayTraceResult(RayTraceResult mop, AxisAlignedBB bounds) {
            super(mop, bounds);
        }
    }
}