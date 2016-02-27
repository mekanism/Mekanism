package mekanism.common.content.boiler;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer
{
	public static double CASING_INSULATION_COEFFICIENT = 50;
	public static double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;
	
	public FluidStack waterStored;
	public FluidStack prevWater;

	public FluidStack steamStored;
	public FluidStack prevSteam;

	public double temperature;

	public double heatToAbsorb;

	public double heatCapacity = 1000;

	public double enthalpyOfVaporization = 10;
	
	public int superheatingElements;
	
	public int waterVolume;
	
	public int steamVolume;

	public ContainerEditMode editMode = ContainerEditMode.BOTH;

	public ItemStack[] inventory = new ItemStack[2];

	public Set<ValveData> valves = new HashSet<ValveData>();
	
	public boolean needsRenderUpdate()
	{
		if((waterStored == null && prevWater != null) || (waterStored != null && prevWater == null))
		{
			return true;
		}
		
		if(waterStored != null && prevWater != null)
		{
			if((waterStored.getFluid() != prevWater.getFluid()) || (waterStored.amount != prevWater.amount))
			{
				return true;
			}
		}
		
		if((steamStored == null && prevSteam != null) || (steamStored != null && prevSteam == null))
		{
			return true;
		}
		
		if(steamStored != null && prevSteam != null)
		{
			if((steamStored.getFluid() != prevSteam.getFluid()) || (steamStored.amount != prevSteam.amount))
			{
				return true;
			}
		}
		
		return false;
	}

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
		return CASING_INVERSE_CONDUCTION_COEFFICIENT*locations.size();
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return CASING_INSULATION_COEFFICIENT*locations.size();
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		double invConduction = IHeatTransfer.AIR_INVERSE_COEFFICIENT + (CASING_INSULATION_COEFFICIENT + CASING_INVERSE_CONDUCTION_COEFFICIENT)*locations.size();
		double heatToTransfer = temperature / invConduction;
		transferHeatTo(-heatToTransfer);
		
		return new double[] {0, heatToTransfer};
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += heatToAbsorb / locations.size();
		heatToAbsorb = 0;
		
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
		
		public boolean prevActive;
		public int activeTicks;
		
		public void onTransfer()
		{
			activeTicks = 30;
		}

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
