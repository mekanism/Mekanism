/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.gates;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipe;

public interface IGate {
	@Deprecated
	void setPulsing(boolean pulse);
	
	ForgeDirection getSide();

	IPipe getPipe();
}
