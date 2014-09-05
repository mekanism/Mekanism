package mekanism.common.recipe.machines;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.outputs.GasOutput;

public class AmbientGasRecipe extends MachineRecipe<IntegerInput, GasOutput, AmbientGasRecipe>
{
	public AmbientGasRecipe(IntegerInput input, GasOutput output)
	{
		super(input, output);
	}

	public AmbientGasRecipe(int input, String output)
	{
		this(new IntegerInput(input), new GasOutput(new GasStack(GasRegistry.getGas(output), 1)));
	}

	public AmbientGasRecipe copy()
	{
		return new AmbientGasRecipe(getInput().copy(), getOutput().copy());
	}
}
