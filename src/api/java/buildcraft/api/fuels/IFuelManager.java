/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.fuels;

import java.util.Collection;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IFuelManager {
    IFuel addFuel(IFuel fuel);

    IFuel addFuel(Fluid fluid, int powerPerCycle, int totalBurningTime);

    IDirtyFuel addDirtyFuel(Fluid fuel, int powerPerCycle, int totalBurningTime, FluidStack residue);

    Collection<IFuel> getFuels();

    IFuel getFuel(Fluid fluid);

    public interface IDirtyFuel extends IFuel {
        /** @return The residue fluidstack, per bucket of original fuel. */
        FluidStack getResidue();
    }
}
