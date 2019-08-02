package mekanism.generators.common.item.generator;

import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.block.generator.BlockTurbineVent;

public class ItemBlockTurbineVent extends ItemBlockTooltip implements IItemSustainedInventory {

    public ItemBlockTurbineVent(BlockTurbineVent block) {
        super(block);
    }
}