package mekanism.common.tile.interfaces;

import java.util.Map;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITileNeighborCache {

    public default void createNeighborCache() {
        for (Direction side : Direction.values()) {
            updateNeighborCache(getPos().offset(side));
        }
    }

    public default void updateNeighborCache(BlockPos neighborPos) {
        BlockState state = Blocks.AIR.getDefaultState();
        if (MekanismUtils.isBlockLoaded(getWorld(), neighborPos)) {
            state = getWorld().getBlockState(neighborPos);
        }
        getNeighborCache().put(neighborPos, state);
    }

    public BlockPos getPos();

    public World getWorld();

    public Map<BlockPos, BlockState> getNeighborCache();
}
