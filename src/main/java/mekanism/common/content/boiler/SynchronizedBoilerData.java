package mekanism.common.content.boiler;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer
{
	public FluidStack waterStored;

	public FluidStack steamStored;

	public double temperature;

	public double heatToAbsorb;

	public double heatCapacity = 0.000001;

	public double enthalpyOfVaporization = 10;

	public ContainerEditMode editMode = ContainerEditMode.BOTH;

	public ItemStack[] inventory = new ItemStack[2];

	public Set<ValveData> valves = new HashSet<ValveData>();

	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return 1;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return 100;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb = heat;
	}

	@Override
	public double[] simulateHeat()
	{
		return new double[0];
	}

	@Override
	public double applyTemperatureChange()
	{

		if(temperature < 100 + IHeatTransfer.AMBIENT_TEMP)
		{
			double temperatureDeficit = 100 + IHeatTransfer.AMBIENT_TEMP - temperature;
			double heatNeeded = temperatureDeficit * volume * heatCapacity * 16000;
			double heatProvided = Math.min(heatToAbsorb, heatNeeded);
			heatToAbsorb -= heatProvided;
			temperature += heatProvided / (volume * heatCapacity * 16);
		}
		if(temperature >= 100 + IHeatTransfer.AMBIENT_TEMP && waterStored != null)
		{
			int amountToBoil = (int)Math.floor(heatToAbsorb / enthalpyOfVaporization);
			amountToBoil = Math.min(amountToBoil, waterStored.amount);
			waterStored.amount -= amountToBoil;
			if(steamStored == null)
			{
				steamStored = new FluidStack(FluidRegistry.getFluid("steam"), amountToBoil);
			}
			else
			{
				steamStored.amount += amountToBoil;
			}

			heatToAbsorb -= amountToBoil * enthalpyOfVaporization;
		}
		heatToAbsorb *= 0.8;
		return temperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return false;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		return null;
	}

	public static class ValveData
	{
		public ForgeDirection side;
		public Coord4D location;
		public boolean serverFluid;

		@Override
		public int hashCode()
		{
			int code = 1;
			code = 31 * code + side.ordinal();
			code = 31 * code + location.hashCode();
			return code;
		}

		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof ValveData && ((ValveData)obj).side == side && ((ValveData)obj).location.equals(location);
		}
	}
}
