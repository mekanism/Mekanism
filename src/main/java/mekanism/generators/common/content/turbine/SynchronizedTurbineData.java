package mekanism.generators.common.content.turbine;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.SynchronizedData;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTurbineData extends SynchronizedData<SynchronizedTurbineData>
{
	public FluidStack fluidStored;
	
	public FluidStack prevFluid;
	
	public double electricityStored;
	
	public int blades;
	public int vents;
	public int coils;
	
	public int lowerVolume;
	
	public Coord4D complex;
	
	public int lastSteamInput;
	
	public int clientDispersers;
	public int clientFlow;
	
	public int getDispersers()
	{
		return (volLength-2)*(volWidth-2) - 1;
	}
	
	public int getFluidCapacity()
	{
		return lowerVolume*TurbineUpdateProtocol.FLUID_PER_TANK;
	}
	
	public double getEnergyCapacity()
	{
		return volume*16000000; //16 MJ energy capacity per volume
	}
	
	public boolean needsRenderUpdate()
	{
		if((fluidStored == null && prevFluid != null) || (fluidStored != null && prevFluid == null))
		{
			return true;
		}
		
		if(fluidStored != null && prevFluid != null)
		{
			if((fluidStored.getFluid() != prevFluid.getFluid()) || (fluidStored.amount != prevFluid.amount))
			{
				return true;
			}
		}
		
		return false;
	}
}
