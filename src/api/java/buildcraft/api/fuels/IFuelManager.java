/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.fuels;

import java.util.Collection;

import net.minecraftforge.fluids.Fluid;

public interface IFuelManager {
	IFuel addFuel(IFuel fuel);

	IFuel addFuel(Fluid fluid, int powerPerCycle, int totalBurningTime);

	Collection<IFuel> getFuels();

	IFuel getFuel(Fluid fluid);
}
