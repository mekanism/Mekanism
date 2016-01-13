package mekanism.common.content.boiler;

import mekanism.common.tile.TileEntityThermoelectricBoiler;

import net.minecraftforge.fluids.FluidStack;

public class BoilerSteamTank extends BoilerTank
{
	public BoilerSteamTank(TileEntityThermoelectricBoiler tileEntity)
	{
		super(tileEntity);
	}

	@Override
	public FluidStack getFluid()
	{
		return steamBoiler.structure != null ? steamBoiler.structure.steamStored : null;
	}

	public void setFluid(FluidStack stack)
	{
		steamBoiler.structure.steamStored = stack;
	}

}
