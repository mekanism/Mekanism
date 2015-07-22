/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.transport;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IGate;

public interface IPipe {
	IPipeTile getTile();

	IGate getGate(ForgeDirection side);
	
	boolean hasGate(ForgeDirection side);
	
	boolean isWired(PipeWire wire);
	
	boolean isWireActive(PipeWire wire);
}
