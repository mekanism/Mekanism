package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class InternalInventorySlot extends BasicInventorySlot {

    public static InternalInventorySlot of(int stackLimit) {
        return new InternalInventorySlot(stackLimit);
    }

    private InternalInventorySlot(int stackLimit) {
        super(stackLimit, alwaysFalse, alwaysFalse, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(int index) {
        return null;
    }
}