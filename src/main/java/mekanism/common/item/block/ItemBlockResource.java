package mekanism.common.item.block;

import mekanism.common.block.basic.BlockResource;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.item.ItemStack;

public class ItemBlockResource extends ItemBlockMekanism<BlockResource> {

    public ItemBlockResource(BlockResource block) {
        super(block);
    }

    @Override
    public int getBurnTime(ItemStack itemStack) {
        // If this is a block of charcoal, set burn time to 16000 ticks (per Minecraft standard)
        //TODO - V10: Move burn time into the resource info
        if (getBlock().getResourceInfo() == BlockResourceInfo.CHARCOAL) {
            return 16000; // ticks
        }
        return super.getBurnTime(itemStack);
    }
}