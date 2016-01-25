/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.tools;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

/*** Implement this interface on subclasses of Item to have that item work as a wrench for buildcraft */
public interface IToolWrench {

    /*** Called to ensure that the wrench can be used. To get the ItemStack that is used, check
     * player.inventory.getCurrentItem()
     *
     * @param player - The player doing the wrenching
     * @param pos - The coordinates for the block being wrenched
     *
     * @return true if wrenching is allowed, false if not */
    boolean canWrench(EntityPlayer player, BlockPos pos);

    /*** Callback after the wrench has been used. This can be used to decrease durability or for other purposes. To get
     * the ItemStack that was used, check player.inventory.getCurrentItem()
     *
     * @param player - The player doing the wrenching
     * @param pos - The position of the block being wrenched */
    void wrenchUsed(EntityPlayer player, BlockPos pos);

    boolean canWrench(EntityPlayer player, Entity entity);

    void wrenchUsed(EntityPlayer player, Entity entity);
}
