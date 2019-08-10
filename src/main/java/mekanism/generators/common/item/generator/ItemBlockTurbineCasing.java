package mekanism.generators.common.item.generator;

import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.generators.common.block.turbine.BlockTurbineCasing;

public class ItemBlockTurbineCasing extends ItemBlockTooltip<BlockTurbineCasing> implements IItemSustainedInventory {

    public ItemBlockTurbineCasing(BlockTurbineCasing block) {
        super(block);
    }
}