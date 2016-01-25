/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.item.ItemStack;

/** Provide requests of items that need to be fulfilled.
 *
 * Requests are organized as an linear array, where null entries mark slots without a requests. A request in a slot, or
 * the amount of slots, is allowed to change before a call to {@link #offerItem(int, ItemStack)}, but it is not
 * recommended that this is frequent, since the request delivery won't fail until it is offered the previous request. */
public interface IRequestProvider {
    /** Return the total number of request slots available from this provider.
     *
     * @return */
    int getRequestsCount();

    /** Return a stack with the request in the slot.
     *
     * @param slot
     * @return the request in the slot, or null if there's no request. */
    ItemStack getRequest(int slot);

    /** Fulfill the request in slot with the stack given and return any excess.
     *
     * @param slot
     * @param stack
     * @return any excess that was not used to fulfill the request. */
    ItemStack offerItem(int slot, ItemStack stack);
}
