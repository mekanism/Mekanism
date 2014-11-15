/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

import net.minecraft.item.ItemStack;

public interface IInvSlot {
    /**
     * Returns the slot number of the underlying Inventory.
     *
     * @return the slot number
     */
    int getIndex();

    boolean canPutStackInSlot(ItemStack stack);

    boolean canTakeStackFromSlot(ItemStack stack);

	ItemStack decreaseStackInSlot(int amount);

    ItemStack getStackInSlot();

    void setStackInSlot(ItemStack stack);
}
