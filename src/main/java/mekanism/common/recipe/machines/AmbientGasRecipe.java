package mekanism.common.recipe.machines;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.IntegerInput;
import mekanism.common.recipe.outputs.GasOutput;

public class AmbientGasRecipe extends MachineRecipe<IntegerInput, GasOutput>
{
	public AmbientGasRecipe(int input, String output)
	{
		super(new IntegerInput(input), new GasOutput(new GasStack(GasRegistry.getGas(output), 1)));
	}
}
