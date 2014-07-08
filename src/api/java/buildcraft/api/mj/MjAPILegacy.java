/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.mj;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;

public class MjAPILegacy implements IPowerReceptor {
	private final PowerHandler powerHandler;
	private final World world;

	protected MjAPILegacy(World world, IBatteryObject battery, PowerHandler.Type type) {
		if (battery == null) {
			throw new NullPointerException();
		}
		this.world = world;
		this.powerHandler = new PowerHandler(this, type, battery);
	}

	public static MjAPILegacy from(World world, IBatteryObject battery, PowerHandler.Type type) {
		if (battery == null) {
			return null;
		}
		return new MjAPILegacy(world, battery, type);
	}

	public static MjAPILegacy from(World world, Object object, PowerHandler.Type type) {
		return from(world, MjAPI.getMjBattery(object), type);
	}

	public static MjAPILegacy from(TileEntity tileEntity, PowerHandler.Type type) {
		return from(tileEntity.getWorldObj(), MjAPI.getMjBattery(tileEntity), type);
	}

	@Override
	public PowerHandler.PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {

	}

	@Override
	public World getWorld() {
		return world;
	}
}
