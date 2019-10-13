package mekanism.common.inventory.slot;

public class OutputInventorySlot extends BasicInventorySlot {

    public OutputInventorySlot() {
        super(item -> true, item -> false);
    }
}