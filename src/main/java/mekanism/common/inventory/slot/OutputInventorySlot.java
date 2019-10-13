package mekanism.common.inventory.slot;

import mekanism.common.inventory.container.slot.ContainerSlotType;

public class OutputInventorySlot extends BasicInventorySlot {

    public static OutputInventorySlot at(int x, int y) {
        return new OutputInventorySlot(x, y);
    }

    private OutputInventorySlot(int x, int y) {
        super(item -> true, item -> false, x, y);
    }

    @Override
    protected ContainerSlotType getSlotType() {
        return ContainerSlotType.OUTPUT;
    }
}