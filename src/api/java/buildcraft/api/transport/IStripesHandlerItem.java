/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStripesHandlerItem {

    /** Called to handle the given {@link ItemStack} within the world. Note that the player's inventory will be empty,
     * except that the target stack will be set into its {@link EnumHand#MAIN_HAND}. Any items left in the players
     * inventory will be returned back through the activator with
     * {@link IStripesActivator#sendItem(ItemStack, EnumFacing)}
     * 
     * @param world
     * @param pos
     * @param direction
     * @param stack The {@link ItemStack} being used
     * @param player
     * @param activator
     * @return True if this used the item, false otherwise (note that this handler MUST NOT return false if it has
     *         changed the world in any way) */
    boolean handle(World world, BlockPos pos, EnumFacing direction, ItemStack stack, EntityPlayer player, IStripesActivator activator);
}
