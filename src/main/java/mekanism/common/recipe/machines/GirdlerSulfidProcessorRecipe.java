package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.outputs.FluidOutput;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class GirdlerSulfidProcessorRecipe extends GspMachineRecipe<FluidInput, FluidInput, FluidOutput, FluidOutput, GirdlerSulfidProcessorRecipe>
{
	public GirdlerSulfidProcessorRecipe(FluidStack input, FluidStack inputsnd, FluidStack output, FluidStack outputsnd)
	{
		super(new FluidInput(input), new FluidInput(inputsnd), new FluidOutput(output), new FluidOutput(outputsnd));
	}

	public GirdlerSulfidProcessorRecipe(FluidInput input, FluidInput inputsnd, FluidOutput output, FluidOutput outputsnd)
	{
		super(input, inputsnd, output, outputsnd);
	}

	@Override
	public GirdlerSulfidProcessorRecipe copy()
	{
		return new GirdlerSulfidProcessorRecipe(getInput(), getInputSnd(), getOutput(), getOutputSnd());
	}

	public boolean canOperate(FluidTank inputTank, FluidTank inputTankSnd, FluidTank outputTank, FluidTank outputTankSnd)
	{
		return getInput().useFluid(inputTank, false, 1) && getInputSnd().useFluid(inputTankSnd, false, 1) && getOutput().applyOutputs(outputTank, false) && getOutputSnd().applyOutputs(outputTankSnd, false);
	}

	public void operate(FluidTank inputTank, FluidTank inputTankSnd, FluidTank outputTank, FluidTank outputTankSnd)
	{
		if(getInput().useFluid(inputTank, true, 1) && getInputSnd().useFluid(inputTankSnd, true, 1))
		{
            getOutput().applyOutputs(outputTank, true);
            getOutputSnd().applyOutputs(outputTankSnd, true);
		}
	}
}
