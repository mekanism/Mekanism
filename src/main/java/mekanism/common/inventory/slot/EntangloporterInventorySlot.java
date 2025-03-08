package mekanism.common.inventory.slot;

import mekanism.api.IContentsListener;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntangloporterInventorySlot extends BasicInventorySlot {

    @NotNull
    public static EntangloporterInventorySlot create(@Nullable IContentsListener listener) {
        return new EntangloporterInventorySlot(listener);
    }

    private EntangloporterInventorySlot(@Nullable IContentsListener listener) {
        super(ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), listener, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        //Make sure the slot doesn't get added to the container
        return null;
    }
}