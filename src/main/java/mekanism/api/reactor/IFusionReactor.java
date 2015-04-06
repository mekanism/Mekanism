package mekanism.api.reactor;

import mekanism.api.IHeatTransfer;
import mekanism.api.gas.GasTank;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;

public interface IFusionReactor extends IHeatTransfer
{
	public void addTemperatureFromEnergyInput(double energyAdded);

	public void simulate();

	public FluidTank getWaterTank();

	public FluidTank getSteamTank();

	public GasTank getDeuteriumTank();

	public GasTank getTritiumTank();

	public GasTank getFuelTank();

	public double getBufferedEnergy();

	public void setBufferedEnergy(double energy);

	public double getPlasmaTemp();

	public void setPlasmaTemp(double temp);

	public double getCaseTemp();

	public void setCaseTemp(double temp);

	public double getBufferSize();

	public void formMultiblock();

	public boolean isFormed();

	public void setInjectionRate(int rate);

	public int getInjectionRate();

	public boolean isBurning();

	public void setBurning(boolean burn);

	public int getMinInjectionRate(boolean active);

	public double getMaxPlasmaTemperature(boolean active);

	public double getMaxCasingTemperature(boolean active);

	public double getIgnitionTemperature(boolean active);

	public double getPassiveGeneration(boolean active, boolean current);

	public int getSteamPerTick(boolean current);

	public void updateTemperatures();
	
	public ItemStack[] getInventory();
}
