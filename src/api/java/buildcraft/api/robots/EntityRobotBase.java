/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyStorage;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IZone;

public abstract class EntityRobotBase extends EntityLiving implements IInventory, IFluidHandler {

    public static final int MAX_ENERGY = 100000;
    public static final int SAFETY_ENERGY = MAX_ENERGY / 5;
    public static final int SHUTDOWN_ENERGY = 0;
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

    public abstract int getEnergy();

    public abstract IEnergyStorage getBattery();

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
