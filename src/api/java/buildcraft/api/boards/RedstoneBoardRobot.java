/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
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
