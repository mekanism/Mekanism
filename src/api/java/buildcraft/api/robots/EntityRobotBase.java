/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.items.IItemHandler;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;

public abstract class EntityRobotBase extends EntityLiving implements IItemHandler, IFluidHandlerAdv {

    public static final long MAX_POWER =  5000 * MjAPI.MJ;
    public static final long SAFETY_POWER = MAX_POWER / 5;
    public static final long SHUTDOWN_POWER = 0;
    public static final long NULL_ROBOT_ID = Long.MAX_VALUE;

    public EntityRobotBase(World par1World) {
        super(par1World);
    }

    public abstract void setItemInUse(ItemStack stack);

    public abstract void setItemActive(boolean b);

    public abstract boolean isMoving();

    public abstract DockingStation getLinkedStation();

    public abstract RedstoneBoardRobot getBoard();

    public abstract void aimItemAt(float yaw, float pitch);

    public abstract void aimItemAt(BlockPos pos);

    public abstract float getAimYaw();

    public abstract float getAimPitch();

    public long getPower() {
        return getBattery().getStored();
    }

    public abstract MjBattery getBattery();

    public abstract DockingStation getDockingStation();

    public abstract void dock(DockingStation station);

    public abstract void undock();

    public abstract IZone getZoneToWork();

    public abstract IZone getZoneToLoadUnload();

    public abstract boolean containsItems();

    public abstract boolean hasFreeSlot();

    public abstract void unreachableEntityDetected(Entity entity);

    public abstract boolean isKnownUnreachable(Entity entity);

    public abstract long getRobotId();

    public abstract IRobotRegistry getRegistry();

    public abstract void releaseResources();

    public abstract void onChunkUnload();

    public abstract ItemStack receiveItem(TileEntity tile, ItemStack stack);

    public abstract void setMainStation(DockingStation station);
}
