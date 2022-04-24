package mekanism.common.item.block.machine;

import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;

public class ItemBlockMachine extends ItemBlockTooltip<BlockTile<?, ?>> implements IItemSustainedInventory {

    public ItemBlockMachine(BlockTile<?, ?> block) {
        super(block);
    }
}