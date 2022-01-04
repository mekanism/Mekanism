package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;

/**
 * Helper marker class for telling apart the main inventory while attempting to move items
 */
public class MainInventorySlot extends InsertableSlot {

    public MainInventorySlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}