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
public class BlockInteractionEvent extends Event {
	public EntityPlayer player;
	public Block block;
	public int meta;

	public BlockInteractionEvent(EntityPlayer player, Block block) {
		this.player = player;
		this.block = block;
	}

	public BlockInteractionEvent(EntityPlayer player, Block block, int meta) {
		this.player = player;
		this.block = block;
		this.meta = meta;
	}
}
