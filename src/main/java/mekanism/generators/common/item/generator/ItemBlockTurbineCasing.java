package mekanism.generators.common.item.generator;

import mekanism.common.base.ISustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.ItemDataUtils;
import mekanism.generators.common.block.generator.BlockTurbineCasing;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class ItemBlockTurbineCasing extends ItemBlockTooltip implements ISustainedInventory {

    public ItemBlockTurbineCasing(BlockTurbineCasing block) {
        super(block);
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }
}