package mekanism.common.inventory.container.slot;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IInsertableSlot {

    //TODO: Improve these java docs at some point

    /**
     * Basically a container slot's equivalent of {@link IInventorySlot#insertItem(ItemStack, Action, AutomationType)} with {@link AutomationType#MANUAL}
     */
    @NotNull
    ItemStack insertItem(@NotNull ItemStack stack, Action action);

    /**
     * Used for determining if this slot can merge with the given stack when the stack is double-clicked.
     */
    default boolean canMergeWith(@NotNull ItemStack stack) {
        return true;
    }

    /**
     * Used for determining if this slot "exists" when a given window is selected.
     *
     * @param windowData Data for currently selected "popup" window or null if there is no window visible.
     */
    default boolean exists(@Nullable SelectedWindowData windowData) {
        return true;
    }
}