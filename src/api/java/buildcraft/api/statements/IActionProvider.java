/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.statements;

import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface IActionProvider {

	/**
	 * Returns the list of actions that are available from the statement container holding the
	 * gate.
	 */
	Collection<IActionInternal> getInternalActions(IStatementContainer container);

	/**
	 * Returns the list of actions available to a gate next to the given block.
	 */
	Collection<IActionExternal> getExternalActions(ForgeDirection side, TileEntity tile);
}
