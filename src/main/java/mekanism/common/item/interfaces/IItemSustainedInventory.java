package mekanism.common.item.interfaces;

import mekanism.api.NBTConstants;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public interface IItemSustainedInventory extends ISustainedInventory {

    @Override
    default void setInventory(ListTag nbtTags, Object... data) {
        if (data[0] instanceof ItemStack stack) {
            ItemDataUtils.setListOrRemove(stack, NBTConstants.ITEMS, nbtTags);
        }
    }

    @Override
    default ListTag getInventory(Object... data) {
        if (data[0] instanceof ItemStack stack) {
            return ItemDataUtils.getList(stack, NBTConstants.ITEMS);
        }
        return null;
    }

    default boolean canContentsDrop(ItemStack stack) {
        return true;
    }
}