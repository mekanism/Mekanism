/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IStripesHandler {
	enum StripesHandlerType {
		ITEM_USE,
		BLOCK_BREAK
	}
	
	StripesHandlerType getType();
	
	boolean shouldHandle(ItemStack stack);
	
	boolean handle(World world, int x, int y, int z, ForgeDirection direction,
			ItemStack stack, EntityPlayer player, IStripesActivator activator);
}
