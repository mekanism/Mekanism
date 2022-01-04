package mekanism.common.tile.interfaces;

import java.util.Map;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public interface ITileNeighborCache extends ITileWrapper {

    default void createNeighborCache() {
        for (Direction side : EnumUtils.DIRECTIONS) {
            updateNeighborCache(getTilePos().relative(side));
        }
    }

    default void updateNeighborCache(BlockPos neighborPos) {
        BlockState state = WorldUtils.getBlockState(getTileWorld(), neighborPos).orElseGet(Blocks.AIR::defaultBlockState);
        getNeighborCache().put(neighborPos, state);
    }

    Map<BlockPos, BlockState> getNeighborCache();
}
