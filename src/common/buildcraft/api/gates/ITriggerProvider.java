/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.gates;

import java.util.LinkedList;

import buildcraft.api.transport.IPipe;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;

public interface ITriggerProvider {

	/**
	 * Returns the list of triggers that are available from the pipe holding the
	 * gate.
	 */
	public abstract LinkedList<ITrigger> getPipeTriggers(IPipe pipe);

	/**
	 * Returns the list of triggers available to a gate next to the given block.
	 */
	public abstract LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile);

}
