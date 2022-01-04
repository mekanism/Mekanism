package mekanism.common.util;

import java.util.Collection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.apache.commons.lang3.tuple.Pair;

public final class MultipartUtils {

    private MultipartUtils() {
    }

    /* taken from MCMP */
    public static Pair<Vec3, Vec3> getRayTraceVectors(Entity entity) {
        float pitch = entity.getXRot();
        float yaw = entity.getYRot();
        Vec3 start = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        float f1 = Mth.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = Mth.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f3 = -Mth.cos(-pitch * 0.017453292F);
        float lookY = Mth.sin(-pitch * 0.017453292F);
        float lookX = f2 * f3;
        float lookZ = f1 * f3;
        double reach = 5.0D;
        if (entity instanceof Player) {
            reach = ((Player) entity).getAttributeValue(ForgeMod.REACH_DISTANCE.get());
        }
        Vec3 end = start.add(lookX * reach, lookY * reach, lookZ * reach);
        return Pair.of(start, end);
    }

    public static AdvancedRayTraceResult collisionRayTrace(Entity entity, BlockPos pos, Collection<VoxelShape> boxes) {
        Pair<Vec3, Vec3> vecs = getRayTraceVectors(entity);
        return collisionRayTrace(pos, vecs.getLeft(), vecs.getRight(), boxes);
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