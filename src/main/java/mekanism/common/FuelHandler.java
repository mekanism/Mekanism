package mekanism.common;

import java.util.HashMap;

import buildcraft.api.mj.MjAPI;
import mekanism.api.gas.Gas;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ModAPIManager;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;

public class FuelHandler
{
	public static HashMap<String, FuelGas> fuels = new HashMap<>();

	public static void addGas(Gas gas, int burnTicks, double energyPerMilliBucket)
	{
		fuels.put(gas.getName(), new FuelGas(burnTicks, energyPerMilliBucket));
	}

	public static FuelGas getFuel(Gas gas)
	{
		if(fuels.containsKey(gas.getName()))
		{
			return fuels.get(gas.getName());
		}

		return null;
	}

	public static class FuelGas
	{
		public int burnTicks;
		public double energyPerTick;

		public FuelGas(int duration, double energyDensity)
		{
			burnTicks = duration;
			energyPerTick = energyDensity / duration;
		}

	}

	public static boolean BCPresent()
	{
		return ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|fuels") && MekanismUtils.classExists("buildcraft.api.fuels.BuildcraftFuelRegistry") && MekanismUtils.classExists("buildcraft.api.fuels.IFuel");
	}
}
