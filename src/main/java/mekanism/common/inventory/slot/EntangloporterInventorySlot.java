package mekanism.common.inventory.slot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class EntangloporterInventorySlot extends BasicInventorySlot {

    @Nonnull
    public static EntangloporterInventorySlot create(@Nullable IMekanismInventory inventory) {
        return new EntangloporterInventorySlot(inventory);
    }

    private EntangloporterInventorySlot(@Nullable IMekanismInventory inventory) {
        super(alwaysTrueBi, alwaysTrueBi, alwaysTrue, inventory, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        //Make sure the slot doesn't get added to the container
        return null;
    }
}