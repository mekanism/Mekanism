package mekanism.common.inventory.slot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class InternalInventorySlot extends BasicInventorySlot {

    @Nonnull
    public static InternalInventorySlot create(@Nullable IContentsListener listener) {
        return new InternalInventorySlot(listener);
    }

    private InternalInventorySlot(@Nullable IContentsListener listener) {
        super(internalOnly, internalOnly, alwaysTrue, listener, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }
}