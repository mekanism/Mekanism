package mekanism.common.inventory.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;

public class OffhandSlot extends InsertableSlot {

    public OffhandSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        setBackground(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
    }
}