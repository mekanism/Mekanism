/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ITriggerProvider {

	/**
	 * Returns the list of triggers that are available from the object holding the gate.
	 */
	Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container);

	/**
	 * Returns the list of triggers available to a gate next to the given block.
	 */
	Collection<ITriggerExternal> getExternalTriggers(ForgeDirection side, TileEntity tile);

}
