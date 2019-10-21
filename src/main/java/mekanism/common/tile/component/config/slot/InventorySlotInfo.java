package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.List;
import mekanism.api.inventory.slot.IInventorySlot;

public class InventorySlotInfo implements ISlotInfo {

    private final List<? extends IInventorySlot> inventorySlots;

    public InventorySlotInfo(IInventorySlot... slots) {
        this(Arrays.asList(slots));
    }

    public InventorySlotInfo(List<? extends IInventorySlot> slots) {
        inventorySlots = slots;
    }

    public List<? extends IInventorySlot> getSlots() {
        return inventorySlots;
    }

    public boolean hasSlot(IInventorySlot slot) {
        //TODO: Check if this even works
        return inventorySlots.contains(slot);
    }
}