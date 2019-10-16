package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class InternalInventorySlot extends BasicInventorySlot {

    public static InternalInventorySlot create() {
        return new InternalInventorySlot();
    }

    private InternalInventorySlot() {
        super(alwaysFalse, alwaysFalse, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot(int index) {
        return null;
    }
}