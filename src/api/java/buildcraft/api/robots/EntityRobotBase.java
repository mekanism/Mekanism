/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyStorage;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.IZone;

public abstract class EntityRobotBase extends EntityLiving implements IInventory, IFluidHandler {

	public static final int MAX_ENERGY = 100000;
	public static final int SAFETY_ENERGY = MAX_ENERGY / 4;
	public static final long NULL_ROBOT_ID = Long.MAX_VALUE;

	public EntityRobotBase(World par1World) {
		super(par1World);
	}

	public abstract void setItemInUse(ItemStack stack);

	public abstract void setItemActive(boolean b);

	public abstract boolean isMoving();

	public abstract IDockingStation getLinkedStation();

	public abstract RedstoneBoardRobot getBoard();

	public abstract void aimItemAt(int x, int y, int z);

	public abstract int getEnergy();

	public abstract IEnergyStorage getBattery();

	public abstract IDockingStation getDockingStation();

	public abstract void dock(IDockingStation station);

	public abstract void undock();

	public abstract IZone getZoneToWork();

	public abstract boolean containsItems();

	public abstract boolean hasFreeSlot();

	public abstract void unreachableEntityDetected(Entity entity);

	public abstract boolean isKnownUnreachable(Entity entity);

	public abstract long getRobotId();

	public abstract IRobotRegistry getRegistry();

	public abstract void releaseResources();
}
