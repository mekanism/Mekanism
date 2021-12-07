package mekanism.common.util;

import java.util.Collection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;
import org.apache.commons.lang3.tuple.Pair;

public final class MultipartUtils {

    private MultipartUtils() {
    }

    /* taken from MCMP */
    public static Pair<Vector3d, Vector3d> getRayTraceVectors(Entity entity) {
        float pitch = entity.xRot;
        float yaw = entity.yRot;
        Vector3d start = new Vector3d(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float lookY = MathHelper.sin(-pitch * 0.017453292F);
        float lookX = f2 * f3;
        float lookZ = f1 * f3;
        double reach = 5.0D;
        if (entity instanceof PlayerEntity) {
            reach = ((PlayerEntity) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        }
        Vector3d end = start.add(lookX * reach, lookY * reach, lookZ * reach);
        return Pair.of(start, end);
    }

    public static AdvancedRayTraceResult collisionRayTrace(Entity entity, BlockPos pos, Collection<VoxelShape> boxes) {
        Pair<Vector3d, Vector3d> vecs = getRayTraceVectors(entity);
        return collisionRayTrace(pos, vecs.getLeft(), vecs.getRight(), boxes);
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vector3d start, Vector3d end, Collection<VoxelShape> boxes) {
        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;
        int i = -1;
        for (VoxelShape shape : boxes) {
            if (shape != null) {
                BlockRayTraceResult result = shape.clip(start, end, pos);
                if (result != null) {
                    result.subHit = i;
                    result.hitInfo = null;
                    AdvancedRayTraceResult advancedResult = new AdvancedRayTraceResult(result, shape);
                    double d = advancedResult.squareDistanceTo(start);
                    if (d < minDistance) {
                        minDistance = d;
                        hit = advancedResult;
                    }
                }
            }
            i++;
        }
        return hit;
    }

    public static class AdvancedRayTraceResult {

        public final VoxelShape bounds;
        public final RayTraceResult hit;

        public AdvancedRayTraceResult(RayTraceResult mop, VoxelShape shape) {
            hit = mop;
            bounds = shape;
        }

        public boolean valid() {
            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vector3d vec) {
            return hit.getLocation().distanceToSqr(vec);
        }
    }
}