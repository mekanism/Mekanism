package mekanism.common.tile.component.config.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.inventory.slot.IInventorySlot;

public class InventorySlotInfo implements ISlotInfo {

    private final List<IInventorySlot> inventorySlots;

    public InventorySlotInfo() {
        inventorySlots = Collections.emptyList();
    }

    public InventorySlotInfo(IInventorySlot... slots) {
        this(Arrays.asList(slots));
    }

    public InventorySlotInfo(List<IInventorySlot> slots) {
        inventorySlots = slots;
    }

    public List<IInventorySlot> getSlots() {
        return inventorySlots;
    }

    public boolean hasSlot(IInventorySlot slot) {
        //TODO: Check if this even works
        return getSlots().contains(slot);
    }
}