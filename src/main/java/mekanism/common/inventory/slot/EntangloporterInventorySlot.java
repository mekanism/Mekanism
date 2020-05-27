package mekanism.common.inventory.slot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class EntangloporterInventorySlot extends BasicInventorySlot {

    @Nonnull
    public static EntangloporterInventorySlot create(@Nullable IContentsListener listener) {
        return new EntangloporterInventorySlot(listener);
    }

    private EntangloporterInventorySlot(@Nullable IContentsListener listener) {
        super(alwaysTrueBi, alwaysTrueBi, alwaysTrue, listener, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        //Make sure the slot doesn't get added to the container
        return null;
    }
}