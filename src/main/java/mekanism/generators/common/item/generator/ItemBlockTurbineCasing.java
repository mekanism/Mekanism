package mekanism.generators.common.item.generator;

import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.block.generator.BlockTurbineCasing;

public class ItemBlockTurbineCasing extends ItemBlockTooltip implements IItemSustainedInventory {

    public ItemBlockTurbineCasing(BlockTurbineCasing block) {
        super(block);
    }
}