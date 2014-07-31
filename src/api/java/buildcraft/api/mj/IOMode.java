/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

public enum IOMode {
	Both(true, true), Receive(true, false), Send(false, true), None(false, false);

	public final boolean canReceive, canSend;

	IOMode(boolean canReceive, boolean canSend) {
		this.canReceive = canReceive;
		this.canSend = canSend;
	}
}
