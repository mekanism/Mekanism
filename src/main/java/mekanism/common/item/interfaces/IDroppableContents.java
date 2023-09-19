package mekanism.common.item.interfaces;

import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.world.item.ItemStack;

public interface IDroppableContents {

    default boolean canContentsDrop(ItemStack stack) {
        return true;
    }

    /**
     * Helper to get the inventory slots that should have their contents dropped into the world
     *
     * @apiNote Server side only.
     */
    List<IInventorySlot> getDroppedSlots(ItemStack stack);
}