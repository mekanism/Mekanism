package mekanism.common.item.block;

import mekanism.common.block.basic.BlockResource;
import net.minecraft.item.ItemStack;

public class ItemBlockResource extends ItemBlockMekanism<BlockResource> {

    public ItemBlockResource(BlockResource block) {
        super(block);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        return getBlock().getResourceInfo().getBurnTime();
    }
}