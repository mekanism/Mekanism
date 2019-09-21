package mekanism.common.item;

import mekanism.api.sustained.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;

public interface IItemSustainedInventory extends ISustainedInventory {

    @Override
    default void setInventory(ListNBT nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    default ListNBT getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }
}