package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.outputs.FluidOutput;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class SolarEvaporationRecipe extends MachineRecipe<FluidInput, FluidOutput, SolarEvaporationRecipe>
{
	public SolarEvaporationRecipe(FluidStack input, FluidStack output)
	{
		super(new FluidInput(input), new FluidOutput(output));
	}

	public SolarEvaporationRecipe(FluidInput input, FluidOutput output)
	{
		super(input, output);
	}

	@Override
	public SolarEvaporationRecipe copy()
	{
		return new SolarEvaporationRecipe(getInput(), getOutput());
	}

	public boolean canOperate(FluidTank inputTank, FluidTank outputTank)
	{
		return getInput().useFluid(inputTank, false, 1) && getOutput().applyOutputs(outputTank, false);
	}

	public void operate(FluidTank inputTank, FluidTank outputTank)
	{
		if(getInput().useFluid(inputTank, true, 1))
		{
			getOutput().applyOutputs(outputTank, true);
		}
	}
}
