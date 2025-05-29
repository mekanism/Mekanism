package mekanism.common.util;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MultipartUtils {

    private MultipartUtils() {
    }

    /* taken from MCMP */
    public static RayTraceVectors getRayTraceVectors(Entity entity) {
        float pitch = entity.getXRot();
        float yaw = entity.getYRot();
        Vec3 start = entity.getEyePosition();
        float f1 = Mth.cos(-yaw * 0.017453292F - Mth.PI);
        float f2 = Mth.sin(-yaw * 0.017453292F - Mth.PI);
        float f3 = -Mth.cos(-pitch * 0.017453292F);
        float lookY = Mth.sin(-pitch * 0.017453292F);
        float lookX = f2 * f3;
        float lookZ = f1 * f3;
        double reach = 5.0D;
        if (entity instanceof Player player) {
            reach = player.blockInteractionRange();
        }
        Vec3 end = start.add(lookX * reach, lookY * reach, lookZ * reach);
        return new RayTraceVectors(start, end);
    }

    public static AdvancedRayTraceResult collisionRayTrace(Entity entity, BlockPos pos, Collection<VoxelShape> boxes) {
        RayTraceVectors vecs = getRayTraceVectors(entity);
        return collisionRayTrace(pos, vecs.start(), vecs.end(), boxes);
    }

    public static AdvancedRayTraceResult collisionRayTrace(BlockPos pos, Vec3 start, Vec3 end, Collection<VoxelShape> boxes) {
        double minDistance = Double.POSITIVE_INFINITY;
        AdvancedRayTraceResult hit = null;
        int i = -1;
        for (VoxelShape shape : boxes) {
            if (shape != null) {
                BlockHitResult result = shape.clip(start, end, pos);
                if (result != null) {
                    AdvancedRayTraceResult advancedResult = new AdvancedRayTraceResult(result, shape, i);
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

    public record RayTraceVectors(Vec3 start, Vec3 end) {
    }

    public static class AdvancedRayTraceResult {

        public final VoxelShape bounds;
        public final HitResult hit;
        public final int subHit;

        public AdvancedRayTraceResult(HitResult mop, VoxelShape shape, int subHit) {
            hit = mop;
            bounds = shape;
            this.subHit = subHit;
        }

        public boolean valid() {
            return hit != null && bounds != null;
        }

        public double squareDistanceTo(Vec3 vec) {
            return hit.getLocation().distanceToSqr(vec);
        }
    }
}