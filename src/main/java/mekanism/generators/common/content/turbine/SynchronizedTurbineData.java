package mekanism.generators.common.content.turbine;

import mekanism.common.multiblock.SynchronizedData;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTurbineData extends SynchronizedData<SynchronizedTurbineData>
{
	public FluidStack fluidStored;
	
	public FluidStack prevFluid;
	
	public int blades;
	public int vents;
	public int coils;
	
	public int lowerVolume;
	
	public int getFluidCapacity()
	{
		return lowerVolume*TurbineUpdateProtocol.FLUID_PER_TANK;
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
