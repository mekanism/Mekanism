package mekanism.api.reactor;

import mekanism.api.gas.GasTank;

import net.minecraftforge.fluids.FluidTank;

public interface IFusionReactor
{
	public void addTemperature(double energyAdded);

	public void simulate();

	public FluidTank getWaterTank();

	public FluidTank getSteamTank();

	public GasTank getDeuteriumTank();

	public GasTank getTritiumTank();

	public GasTank getFuelTank();
}
