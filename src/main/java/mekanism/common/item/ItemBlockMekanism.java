package mekanism.common.item;

import mekanism.common.block.IBlockMekanism;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockMekanism<BLOCK extends Block & IBlockMekanism> extends ItemBlock implements IItemMekanism {

    public ItemBlockMekanism(BLOCK block) {
        super(block);
    }
}