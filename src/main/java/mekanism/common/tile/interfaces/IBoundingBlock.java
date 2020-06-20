package mekanism.common.tile.interfaces;

import net.minecraft.block.BlockState;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock {

    /**
     * Called when the main block is placed.
     */
    //TODO: Evaluate this, onPlace is a method in TileEntityMekanism now, so it always gets called via that exposure
    // So having this here may not be needed anymore
    void onPlace();

    /**
     * Called when any part of the structure is broken.
     *
     * @param oldState TODO
     */
    void onBreak(BlockState oldState);
}