package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.outputs.MachineOutput;

public abstract class GspMachineRecipe<INPUT extends MachineInput, INPUTSND extends MachineInput, OUTPUT extends MachineOutput, OUTPUTSND extends MachineOutput, RECIPE extends GspMachineRecipe<INPUT, INPUTSND, OUTPUT, OUTPUTSND, RECIPE>>
{
	public INPUT recipeInput;
    public INPUTSND recipeInputSnd;

	public OUTPUT recipeOutput;
    public  OUTPUTSND recipeOutputSnd;

	public GspMachineRecipe(INPUT input, INPUTSND inputsnd, OUTPUT output, OUTPUTSND outputsnd)
	{
		recipeInput = input;
        recipeInputSnd = inputsnd;
		recipeOutput = output;
        recipeOutputSnd = outputsnd;
	}

	public INPUT getInput()
	{
		return recipeInput;
	}

    public INPUTSND getInputSnd()
    {
        return recipeInputSnd;
    }

    public OUTPUT getOutput()
	{
		return recipeOutput;
	}

    public OUTPUTSND getOutputSnd()
    {
        return recipeOutputSnd;
    }
	public abstract RECIPE copy();
}
