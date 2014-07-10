package mekanism.generators.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.reactor.INeutronCapture;
import mekanism.api.reactor.IReactorBlock;
import mekanism.generators.common.tile.TileEntityReactorController;

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidTank;

import static java.lang.Math.min;
import static java.lang.Math.max;

public class FusionReactor
{
	public static final int MAX_WATER = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public static final int MAX_FUEL = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public FluidTank waterTank = new FluidTank(MAX_WATER);
	public GasTank steamTank = new GasTank(MAX_WATER*1000);

	public GasTank deuteriumTank = new GasTank(MAX_FUEL);
	public GasTank tritiumTank = new GasTank(MAX_FUEL);

	public GasTank fuelTank = new GasTank(MAX_FUEL);

	public TileEntityReactorController controller;
	public Set<IReactorBlock> reactorBlocks = new HashSet<IReactorBlock>();
	public Set<INeutronCapture> neutronCaptors = new HashSet<INeutronCapture>();

	public double temperature;
	public double ignitionTemperature = 10^7;

	public double burnRatio = 1;
	public double tempPerFuel = 100000;
	public int injectionRate;

	public boolean burning = false;
	public boolean hasHohlraum = false;

	public void simulate()
	{
		if(temperature >= ignitionTemperature)
		{
			if(!burning && hasHohlraum)
			{
				vaporiseHohlraum();
			}
			injectFuel();

			int fuelBurned = burnFuel();
			neutronFlux(fuelBurned);
		}
		else {
			burning = false;
		}
		boilWater();
		ambientLoss();
	}

	public void vaporiseHohlraum()
	{
		fuelTank.receive(new GasStack(GasRegistry.getGas("fusionFuel"), 1000), true);
		burning = true;
	}

	public void injectFuel()
	{
		int amountNeeded = fuelTank.getNeeded();
		int amountAvailable = 2*min(deuteriumTank.getStored(), tritiumTank.getStored());
		int amountToInject = min(amountNeeded, min(amountAvailable, injectionRate));
		amountToInject -= amountToInject % 2;
		deuteriumTank.draw(amountToInject/2, true);
		tritiumTank.draw(amountToInject/2, true);
		fuelTank.receive(new GasStack(GasRegistry.getGas("fusionFuel"), amountToInject), true);
	}

	public int burnFuel()
	{
		int fuelBurned = (int)min(fuelTank.getStored(), max(0, temperature-ignitionTemperature)*burnRatio);
		fuelTank.draw(fuelBurned, true);
		temperature += tempPerFuel * fuelBurned;
		return fuelBurned;
	}

	public void neutronFlux(int fuelBurned)
	{
		int neutronsRemaining = fuelBurned;
		List<INeutronCapture> list = new ArrayList<INeutronCapture>(neutronCaptors);
		Collections.shuffle(list);
		for(INeutronCapture captor: neutronCaptors)
		{
			if(neutronsRemaining <= 0)
				break;

			neutronsRemaining = captor.absorbNeutrons(neutronsRemaining);
		}
		controller.radiateNeutrons(neutronsRemaining);
	}

	public void boilWater()
	{
		int waterToBoil = (int)min(waterTank.getFluidAmount(), temperature/1000);
	}

	public void ambientLoss()
	{
		temperature -= 0.1*temperature;
	}
}
