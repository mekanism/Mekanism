package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;

public class OffhandSlot extends InsertableSlot {

    public OffhandSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
    }
}