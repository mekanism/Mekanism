package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;

public class InternalInventorySlot extends BasicInventorySlot {

    public static InternalInventorySlot create(IMekanismInventory inventory) {
        return new InternalInventorySlot(inventory);
    }

    private InternalInventorySlot(IMekanismInventory inventory) {
        super((stack, automationType) -> automationType == AutomationType.INTERNAL, (stack, automationType) -> automationType == AutomationType.INTERNAL, alwaysTrue,
              inventory, 0, 0);
    }

    @Nullable
    @Override
    public InventoryContainerSlot createContainerSlot() {
        return null;
    }
}