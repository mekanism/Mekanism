package mekanism.common.teleportation;

import mekanism.api.gas.GasTank;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidTank;

public class SharedInventory
{
	public String name;

	public double storedEnergy;
	public FluidTank storedFluid;
	public GasTank storedGas;
	public ItemStack storedItem;

	public SharedInventory(String freq)
	{
		name = freq;

		storedEnergy = 0;
		storedFluid = new FluidTank(1000);
		storedGas = new GasTank(1000);
		storedItem = null;
	}
}
