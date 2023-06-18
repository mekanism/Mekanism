package mekanism.common.content.gear;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.RayTraceVectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public interface IBlastingItem {

    Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state);

    static Map<BlockPos, BlockState> findPositions(Level world, BlockPos targetPos, Player player, int radius) {
        if (radius > 0) {
            Direction targetSide = getTargetSide(world, targetPos, player);
            if (targetSide != null) {
                Vec3i lower, upper;
                switch (targetSide) {
                    case UP, DOWN -> {
                        lower = new Vec3i(-radius, 0, -radius);
                        upper = new Vec3i(radius, 0, radius);
                    }
                    case EAST, WEST -> {
                        lower = new Vec3i(0, -1, -radius);
                        upper = new Vec3i(0, 2 * radius - 1, radius);
                    }
                    case NORTH, SOUTH -> {
                        lower = new Vec3i(-radius, -1, 0);
                        upper = new Vec3i(radius, 2 * radius - 1, 0);
                    }
                    default -> {
                        lower = new Vec3i(0, 0, 0);
                        upper = new Vec3i(0, 0, 0);
                    }
                }
                Map<BlockPos, BlockState> found = new HashMap<>();
                for (BlockPos nextPos : BlockPos.betweenClosed(targetPos.offset(lower), targetPos.offset(upper))) {
                    BlockState nextState = world.getBlockState(nextPos);
                    if (canBlastBlock(world, nextPos, nextState)) {
                        found.put(nextPos.immutable(), nextState);
                    }
                }
                return found;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Very watered down version of {@link VoxelShape#clip(Vec3, Vec3, BlockPos)}, that instead of creating an extra voxel shape from the bounds and then checking if it
     * is a full side and using a quicker get nearest, we just do a single call on the overall AABB to get the direction after doing the initial check from
     * {@link VoxelShape#clip(Vec3, Vec3, BlockPos)}. In theory this might be ever so slightly worse performance wise for a full cube, it shouldn't be noticeably worse,
     * and for cases where there are more complex non-full block shapes, then we can skip a handful of checks as well as creating excess objects for the voxel shape and
     * hit result as the only details we care about from the hit result is the direction we hit.
     */
    @Nullable
    private static Direction getTargetSide(Level world, BlockPos targetPos, Player player) {
        RayTraceVectors rayTraceVectors = MultipartUtils.getRayTraceVectors(player);
        Vec3 start = rayTraceVectors.start();
        Vec3 end = rayTraceVectors.end();
        Vec3 distance = end.subtract(start);
        if (distance.lengthSqr() < 1.0E-7D) {
            return null;
        }
        BlockState targetState = world.getBlockState(targetPos);
        VoxelShape shape = targetState.getShape(world, targetPos, CollisionContext.of(player));
        if (!shape.isEmpty()) {
            AABB bounds = shape.bounds();
            double[] ignoredMinDistance = {1.0D};
            return AABB.getDirection(bounds.move(targetPos), start, ignoredMinDistance, null, end.x - start.x, end.y - start.y, end.z - start.z);
        }
        return null;
    }

    static boolean canBlastBlock(Level world, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.liquid() && ModuleVeinMiningUnit.canVeinBlock(state) && state.getDestroySpeed(world, pos) > 0;
    }
}