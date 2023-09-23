package mekanism.common.item.interfaces;

import java.util.Collections;
import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public interface IItemSustainedInventory extends ISustainedInventory, IDroppableContents {

    @Override
    default void setSustainedInventory(ListTag nbtTags) {
        throw new UnsupportedOperationException("IItemSustainedInventory needs a stack to work with");
    }

    default void setSustainedInventory(ListTag nbtTags, ItemStack stack) {
        ItemDataUtils.setListOrRemove(stack, NBTConstants.ITEMS, nbtTags);
    }

    @Override
    default ListTag getSustainedInventory() {
        throw new UnsupportedOperationException("IItemSustainedInventory needs a stack to work with");
    }

    default ListTag getSustainedInventory(ItemStack stack) {
        return ItemDataUtils.getList(stack, NBTConstants.ITEMS);
    }

    /**
     * Gets if there is an inventory from an item or block.
     *
     * @param stack - ItemStack parameter
     *
     * @return true if there is a non-empty inventory stored, false otherwise
     */
    default boolean hasSustainedInventory(ItemStack stack) {
        ListTag inventory = getSustainedInventory(stack);
        return inventory != null && !inventory.isEmpty();
    }

    @Override
    default List<IInventorySlot> getDroppedSlots(ItemStack stack) {
        ListTag inventory = getSustainedInventory(stack);
        return inventory == null ? Collections.emptyList() : ItemRecipeData.readContents(inventory);
    }
}
