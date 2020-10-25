package mekanism.common.tile.interfaces;

import java.util.Map;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface ITileNeighborCache extends ITileWrapper {

    default void createNeighborCache() {
        for (Direction side : EnumUtils.DIRECTIONS) {
            updateNeighborCache(getTilePos().offset(side));
        }
    }

    default void updateNeighborCache(BlockPos neighborPos) {
        BlockState state = WorldUtils.getBlockState(getTileWorld(), neighborPos).orElseGet(Blocks.AIR::getDefaultState);
        getNeighborCache().put(neighborPos, state);
    }

    Map<BlockPos, BlockState> getNeighborCache();
}
