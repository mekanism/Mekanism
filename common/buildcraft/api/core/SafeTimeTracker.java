/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.core;

import net.minecraft.world.World;

public class SafeTimeTracker {

	private long lastMark = 0;

	/**
	 * Return true if a given delay has passed since last time marked was called successfully.
	 */
	public boolean markTimeIfDelay(World world, long delay) {
		if (world == null)
			return false;

		long currentTime = world.getWorldTime();

		if (currentTime < lastMark) {
			lastMark = currentTime;
			return false;
		} else if (lastMark + delay <= currentTime) {
			lastMark = world.getWorldTime();
			return true;
		} else
			return false;

	}

	public void markTime(World world) {
		lastMark = world.getWorldTime();
	}
}
