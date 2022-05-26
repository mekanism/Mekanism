package mekanism.common.content.gear;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.util.MultipartUtils;
import mekanism.common.util.MultipartUtils.RayTraceVectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

public interface IBlastingItem {

    Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state);

    static Map<BlockPos, BlockState> findPositions(Level world, BlockPos targetPos, Player player, int radius) {
        if (radius > 0) {
            RayTraceVectors rayTraceVectors = MultipartUtils.getRayTraceVectors(player);
            BlockHitResult hitResult = Shapes.block().clip(rayTraceVectors.start(), rayTraceVectors.end(), targetPos);
            if (hitResult != null) {
                Vec3i lower, upper;
                switch (hitResult.getDirection()) {
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

    static boolean canBlastBlock(Level world, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.getMaterial().isLiquid() && ModuleVeinMiningUnit.canVeinBlock(state) && state.getDestroySpeed(world, pos) > 0;
    }
}