package mekanism.common.content.gear;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlastingItem {

    Map<BlockPos, BlockState> getBlastedBlocks(Level world, Player player, ItemStack stack, BlockPos pos, BlockState state);

    static Map<BlockPos, BlockState> findPositions(Level world, BlockPos targetPos, Direction axis, int radius) {
        if(radius <= 0) {
            return Collections.emptyMap();
        }
        int minX = 0, minY = 0, minZ = 0;
        int maxX = 0, maxY = 0, maxZ = 0;
        switch (axis) {
            case UP, DOWN -> {
                minX = -radius;
                maxX = radius;
                minZ = -radius;
                maxZ = radius;
            }
            case EAST, WEST -> {
                minY = -1;
                maxY = 2 * radius - 1;
                minZ = -radius;
                maxZ = radius;
            }
            case NORTH, SOUTH -> {
                minX = -radius;
                maxX = radius;
                minY = -1;
                maxY = 2 * radius - 1;
            }
        }
        Map<BlockPos, BlockState> found = new HashMap<>();
        for (BlockPos nextPos : BlockPos.betweenClosed(targetPos.offset(minX, minY, minZ), targetPos.offset(maxX, maxY, maxZ))) {
            BlockState nextState = world.getBlockState(nextPos);
            if (canBlastBlock(world, nextPos, nextState)) {
                found.put(nextPos.immutable(), nextState);
            }
        }
        return found;
    }

    static boolean canBlastBlock(Level world, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.getMaterial().isLiquid() && ModuleVeinMiningUnit.canVeinBlock(state) && state.getDestroySpeed(world, pos) > 0;
    }
}