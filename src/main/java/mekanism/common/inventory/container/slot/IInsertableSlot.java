package mekanism.common.inventory.container.slot;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.item.ItemStack;

public interface IInsertableSlot {

    //TODO: Improve these java docs at some point

    /**
     * Basically a container slot's equivalent of {@link IInventorySlot#insertItem(ItemStack, Action, AutomationType)} with {@link AutomationType#MANUAL}
     */
    @Nonnull
    ItemStack insertItem(@Nonnull ItemStack stack, Action action);

    /**
     * Used for determining if this slot can merge with the given stack when the stack is double clicked.
     */
    default boolean canMergeWith(@Nonnull ItemStack stack) {
        return true;
    }
}