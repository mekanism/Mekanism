package mekanism.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class SlotIgnored extends Slot {

    public SlotIgnored(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}