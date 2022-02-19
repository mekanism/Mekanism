package mekanism.common.tile;

import mekanism.common.registries.MekanismTileEntityTypes;

//TODO - 1.18: Remove advanced bounding blocks
public class TileEntityAdvancedBoundingBlock extends TileEntityBoundingBlock {

    public TileEntityAdvancedBoundingBlock() {
        super(MekanismTileEntityTypes.ADVANCED_BOUNDING_BLOCK.getTileEntityType());
    }
}