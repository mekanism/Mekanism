/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.boards;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobot extends AIRobot implements IRedstoneBoard<EntityRobotBase> {

	public RedstoneBoardRobot(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public abstract RedstoneBoardRobotNBT getNBTHandler();

	@Override
	public final void updateBoard(EntityRobotBase container) {

	}

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

}
