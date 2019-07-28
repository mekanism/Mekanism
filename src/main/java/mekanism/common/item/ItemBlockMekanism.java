package mekanism.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockMekanism extends ItemBlock implements IItemMekanism {

    public ItemBlockMekanism(Block block) {
        super(block);
        //Ensure the name is lower case as with concatenating with values from enums it may not be
        setRegistryName(block.getRegistryName());
    }
}