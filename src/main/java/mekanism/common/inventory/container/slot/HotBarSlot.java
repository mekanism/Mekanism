package mekanism.common.inventory.container.slot;

import net.minecraft.world.Container;

/**
 * Helper marker class for telling apart the hot bar while attempting to move items
 */
public class HotBarSlot extends InsertableSlot {

    public HotBarSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
}