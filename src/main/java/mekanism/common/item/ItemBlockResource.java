package mekanism.common.item;

import mekanism.common.block.basic.BlockResource;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.item.ItemStack;

public class ItemBlockResource extends ItemBlockMekanism<BlockResource> {

    private final BlockResource blockResource;

    public ItemBlockResource(BlockResource block) {
        super(block);
        blockResource = block;
    }

    @Override
    public int getItemBurnTime(ItemStack itemStack) {
        // If this is a block of charcoal, set burn time to 16000 ticks (per Minecraft standard)
        //TODO: Move burn time into the resource info
        if (blockResource.getResourceInfo() == BlockResourceInfo.CHARCOAL) {
            return 16000; // ticks
        }
        return super.getItemBurnTime(itemStack);
    }
}