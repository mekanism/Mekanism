/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.events;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;


@Cancelable
public class BlockPlacedDownEvent extends Event {
	public EntityPlayer player;
	public Block block;
	public int meta, x, y, z;

	public BlockPlacedDownEvent(EntityPlayer player, Block block, int meta, int x, int y, int z) {
		this.player = player;
		this.block = block;
		this.meta = meta;
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
