/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
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
