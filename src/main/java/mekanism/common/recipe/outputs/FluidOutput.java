package mekanism.common.recipe.outputs;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidOutput extends MachineOutput<FluidOutput>
{
	public FluidStack output;

	public FluidOutput(FluidStack stack)
	{
		output = stack;
	}

	@Override
	public FluidOutput copy()
	{
		return new FluidOutput(output.copy());
	}

	public boolean applyOutputs(FluidTank fluidTank, boolean doEmit)
	{
		if(fluidTank.fill(output, false) > 0)
		{
			fluidTank.fill(output, doEmit);
			
			return true;
		}
		
		return false;
	}
}
