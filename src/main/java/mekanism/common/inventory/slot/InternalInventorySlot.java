package mekanism.common.inventory.slot;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import org.jspecify.annotations.Nullable;

public class InternalInventorySlot extends BasicInventorySlot {

    public static InternalInventorySlot create(@Nullable IContentsListener listener) {
        return new InternalInventorySlot(listener);
    }

    private InternalInventorySlot(@Nullable IContentsListener listener) {
        super(ConstantPredicates.internalOnly(), ConstantPredicates.internalOnly(), ConstantPredicates.alwaysTrue(), null, null, listener, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }
}