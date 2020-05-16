package mekanism.common.tile.interfaces;

import java.util.Map;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITileNeighborCache {

    default void createNeighborCache() {
        for (Direction side : Direction.values()) {
            updateNeighborCache(getPos().offset(side));
        }
    }

    default void updateNeighborCache(BlockPos neighborPos) {
        BlockState state = Blocks.AIR.getDefaultState();
        if (MekanismUtils.isBlockLoaded(getWorld(), neighborPos)) {
            state = getWorld().getBlockState(neighborPos);
        }
        getNeighborCache().put(neighborPos, state);
    }

    BlockPos getPos();

    World getWorld();

    Map<BlockPos, BlockState> getNeighborCache();
}
