package mekanism.api.gas;

import java.util.HashMap;

import mekanism.common.Mekanism;

import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.fuels.IronEngineFuel.Fuel;

public class FuelHandler
{
	public static HashMap<Gas, FuelGas> fuels = new HashMap<Gas, FuelGas>();

	public static FuelGas getFuel(Gas gas)
	{
		if(fuels.containsKey(gas))
		{
			return fuels.get(gas);
		}

		if(gas.hasFluid())
		{
			Fuel bcFuel = IronEngineFuel.getFuelForFluid(gas.getFluid());
			if(bcFuel != null)
			{
				FuelGas fuel = new FuelGas(bcFuel);
				fuels.put(gas, fuel);
				return fuel;
			}
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

		public FuelGas(Fuel bcFuel)
		{
			burnTicks = bcFuel.totalBurningTime;
			energyPerTick = bcFuel.powerPerCycle * Mekanism.FROM_BC;
		}
	}
}
