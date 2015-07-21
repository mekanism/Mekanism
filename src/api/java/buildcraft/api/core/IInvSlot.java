/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
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

    boolean isItemValidForSlot(ItemStack stack);

	ItemStack decreaseStackInSlot(int amount);

    ItemStack getStackInSlot();

    void setStackInSlot(ItemStack stack);
}
