package mekanism.generators.common.item.generator;

import mekanism.common.base.ISustainedInventory;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.util.ItemDataUtils;
import mekanism.generators.common.block.generator.BlockTurbineVent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class ItemBlockTurbineVent extends ItemBlockTooltip implements ISustainedInventory {

    public ItemBlockTurbineVent(BlockTurbineVent block) {
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