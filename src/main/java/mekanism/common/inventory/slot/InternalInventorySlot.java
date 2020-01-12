package mekanism.common.inventory.slot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class InternalInventorySlot extends BasicInventorySlot {

    @Nonnull
    public static InternalInventorySlot create(@Nullable IMekanismInventory inventory) {
        return new InternalInventorySlot(inventory);
    }

    private InternalInventorySlot(@Nullable IMekanismInventory inventory) {
        super(internalOnly, internalOnly, alwaysTrue, inventory, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }
}