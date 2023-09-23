package mekanism.common.tile.interfaces;

import net.minecraft.nbt.ListTag;

/**
 * Internal interface used in blocks that are capable of storing sustained inventories.
 * Use {@link mekanism.common.item.interfaces.IItemSustainedInventory} for items
 *
 * @author AidanBrady
 */
public interface ISustainedInventory {

    /**
     * Sets the inventory tag list to a new value.
     *
     * @param nbtTags - NBTTagList value to set
     */
    void setSustainedInventory(ListTag nbtTags);

    /**
     * Gets the inventory tag list from a block.
     * Do not implement for Items
     *
     * @return inventory tag list
     */
    ListTag getSustainedInventory();

    /**
     * Gets if there is an inventory from an item or block.
     * Renamed due to clash with {@link mekanism.api.inventory.IMekanismInventory#hasInventory()},
     * previously was only ever used with items.
     *
     * @return true if there is a non-empty inventory stored, false otherwise
     */
    default boolean hasSustainedInventory() {
        ListTag inventory = getSustainedInventory();
        return inventory != null && !inventory.isEmpty();
    }
}
