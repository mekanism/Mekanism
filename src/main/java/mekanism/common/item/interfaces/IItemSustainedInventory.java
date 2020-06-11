package mekanism.common.item.interfaces;

import mekanism.api.NBTConstants;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;

public interface IItemSustainedInventory extends ISustainedInventory {

    @Override
    default void setInventory(ListNBT nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], NBTConstants.ITEMS, nbtTags);
        }
    }

    @Override
    default ListNBT getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], NBTConstants.ITEMS);
        }
        return null;
    }
}