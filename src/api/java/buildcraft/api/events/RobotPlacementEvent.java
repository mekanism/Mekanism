/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RobotPlacementEvent extends Event {
    public EntityPlayer player;
    public String robotProgram;

    public RobotPlacementEvent(EntityPlayer player, String robotProgram) {
        this.player = player;
        this.robotProgram = robotProgram;
    }

}
