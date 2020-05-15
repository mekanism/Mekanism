package mekanism.common.multiblock;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeMultiblock;
import mekanism.common.multiblock.MultiblockData.BlockLocation;
import mekanism.common.tile.interfaces.ITileNeighborCache;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public interface IMultiblockBase extends ITileNeighborCache {

    MultiblockData getMultiblock();

    void removeMultiblock();

    void markUpdated();

    ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack);

    void doUpdate(BlockPos neighborPos, boolean force);

    default boolean shouldUpdate(BlockPos neighborPos) {
        // if the update call wasn't from a neighbor, run the update
        if (neighborPos == null) {
            return true;
        }
        BlockState state = MekanismUtils.isBlockLoaded(getWorld(), neighborPos) ? getWorld().getBlockState(neighborPos) : Blocks.AIR.getDefaultState();
        BlockState cache = getNeighborCache().get(neighborPos);
        updateNeighborCache(neighborPos);
        if (cache == null) {
            // if the cache was previously non-present, fetch the just-updated one
            cache = getNeighborCache().get(neighborPos);
        } else if (state.getBlock() == cache.getBlock()) {
            // if the cache existed and the block didn't actually change, we don't care about an update
            return false;
        }
        MultiblockData data = getMultiblock();
        if (data.isFormed()) {
            BlockLocation location = data.getBlockLocation(neighborPos);
            boolean isMultiblock = Attribute.has(state.getBlock(), AttributeMultiblock.class);
            // if the update occurred outside of the multiblock, we don't care
            if (location == BlockLocation.OUTSIDE) {
                return false;
            // if it's a part of the multiblock and it's still there, we don't care either
            } else if (location == BlockLocation.WALLS && isMultiblock) {
                return false;
            }
        } else if (!state.isAir(getWorld(), neighborPos) && !Attribute.has(state.getBlock(), AttributeMultiblock.class)) {
            // if we're not formed and the block wouldn't affect a multiblock structure, ignore
            return false;
        }
        return true;
    }
}
