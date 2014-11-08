/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.robots;

import java.util.Collection;

import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.util.ForgeDirection;

public interface IRobotRegistry {

	long getNextRobotId();

	void registerRobot(EntityRobotBase robot);

	void killRobot(EntityRobotBase robot);

	EntityRobotBase getLoadedRobot(long id);

	boolean isTaken(ResourceId resourceId);

	long robotIdTaking(ResourceId resourceId);

	EntityRobotBase robotTaking(ResourceId resourceId);

	boolean take(ResourceId resourceId, EntityRobotBase robot);

	boolean take(ResourceId resourceId, long robotId);

	void release(ResourceId resourceId);

	void releaseResources(EntityRobotBase robot);

	IDockingStation getStation(int x, int y, int z, ForgeDirection side);

	Collection<IDockingStation> getStations();

	void registerStation(IDockingStation station);

	void removeStation(IDockingStation station);

	void take(IDockingStation station, long robotId);

	void release(IDockingStation station, long robotId);

	void writeToNBT(NBTTagCompound nbt);

	void readFromNBT(NBTTagCompound nbt);
}
