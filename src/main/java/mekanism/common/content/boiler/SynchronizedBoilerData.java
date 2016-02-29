package mekanism.common.content.boiler;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismConfig.general;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedBoilerData extends SynchronizedData<SynchronizedBoilerData> implements IHeatTransfer
{
	public static double CASING_INSULATION_COEFFICIENT = 1;
	public static double CASING_INVERSE_CONDUCTION_COEFFICIENT = 1;
	public static double BASE_BOIL_TEMP = 100-(TemperatureUnit.AMBIENT.zeroOffset-TemperatureUnit.CELSIUS.zeroOffset);
	
	public FluidStack waterStored;
	public FluidStack prevWater;

	public FluidStack steamStored;
	public FluidStack prevSteam;
	
	public double lastEnvironmentLoss;
	public int lastBoilRate;
	public int lastMaxBoil;

	public double temperature;

	public double heatToAbsorb;

	public double heatCapacity = 1000;
	
	public int superheatingElements;
	
	public int waterVolume;
	
	public int steamVolume;

	public ContainerEditMode editMode = ContainerEditMode.BOTH;

	public ItemStack[] inventory = new ItemStack[2];
	
	public Coord4D upperRenderLocation;

	public Set<ValveData> valves = new HashSet<ValveData>();
	
	/**
	 * @return how much heat energy is needed to convert one unit of water into steam
	 */
	public static double getHeatEnthalpy()
	{
		return general.maxEnergyPerSteam/general.energyPerHeat;
	}
	
	public double getHeatAvailable()
	{
		double heatAvailable = (temperature-BASE_BOIL_TEMP)*locations.size();
		return Math.min(heatAvailable, superheatingElements*general.superheatingHeatTransfer);
	}
	
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
}
