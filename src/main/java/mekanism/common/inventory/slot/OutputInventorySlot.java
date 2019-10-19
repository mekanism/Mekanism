package mekanism.common.inventory.slot;

import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.container.slot.ContainerSlotType;

public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(IMekanismInventory inventory, int x, int y) {
        return new OutputInventorySlot(inventory, x, y);
    }

    private OutputInventorySlot(IMekanismInventory inventory, int x, int y) {
        super(alwaysTrue, alwaysFalse, alwaysTrue, inventory, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.OUTPUT;
    }
}