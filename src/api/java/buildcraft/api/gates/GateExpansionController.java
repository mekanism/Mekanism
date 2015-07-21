/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.gates;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;

public abstract class GateExpansionController {

	public final IGateExpansion type;
	public final TileEntity pipeTile;

	public GateExpansionController(IGateExpansion type, TileEntity pipeTile) {
		this.pipeTile = pipeTile;
		this.type = type;
	}

	public IGateExpansion getType() {
		return type;
	}

	public boolean isActive() {
		return false;
	}

	public void tick(IGate gate) {
	}

	public void startResolution() {
	}

	public boolean resolveAction(IStatement action, int count) {
		return false;
	}

	public boolean isTriggerActive(IStatement trigger, IStatementParameter[] parameters) {
		return false;
	}

	public void addTriggers(List<ITriggerInternal> list) {
	}

	public void addActions(List<IActionInternal> list) {
	}

	public void writeToNBT(NBTTagCompound nbt) {
	}

	public void readFromNBT(NBTTagCompound nbt) {
	}
}
