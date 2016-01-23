package mekanism.generators.common.content.turbine;

import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class TurbineCache extends MultiblockCache<SynchronizedTurbineData>
{
	public FluidStack fluid;
	
	@Override
	public void apply(SynchronizedTurbineData data)
	{
		data.fluidStored = fluid;
	}

	@Override
	public void sync(SynchronizedTurbineData data) 
	{
		fluid = data.fluidStored;
	}

	@Override
	public void load(NBTTagCompound nbtTags) 
	{
		if(nbtTags.hasKey("cachedFluid"))
		{
			fluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedFluid"));
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags) 
	{
		if(fluid != null)
		{
			nbtTags.setTag("cachedFluid", fluid.writeToNBT(new NBTTagCompound()));
		}
	}
}
