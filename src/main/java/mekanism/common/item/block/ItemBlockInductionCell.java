package mekanism.common.item.block;

import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import net.minecraft.world.item.Item;

public class ItemBlockInductionCell extends ItemBlockTooltip<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>> {

    public ItemBlockInductionCell(BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>> block, Item.Properties properties) {
        super(block, properties.component(MekanismDataComponents.INDUCTION_CELL_TIER, Attribute.getTierNN(block, InductionCellTier.class)));
    }

    @Override
    protected boolean exposesEnergyCap() {
        return false;
    }
}