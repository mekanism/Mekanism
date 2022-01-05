package mekanism.common.item.interfaces;

import mekanism.api.NBTConstants;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.ListTag;

public interface IItemSustainedInventory extends ISustainedInventory {

    @Override
    default void setInventory(ListTag nbtTags, Object... data) {
        if (data[0] instanceof ItemStack stack) {
            ItemDataUtils.setList(stack, NBTConstants.ITEMS, nbtTags);
        }
    }

    @Override
    default ListTag getInventory(Object... data) {
        if (data[0] instanceof ItemStack stack) {
            return ItemDataUtils.getList(stack, NBTConstants.ITEMS);
        }
        return null;
    }
}