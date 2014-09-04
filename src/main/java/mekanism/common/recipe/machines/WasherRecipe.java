package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.outputs.GasOutput;

public class WasherRecipe extends MachineRecipe<GasInput, GasOutput>
{
	public WasherRecipe(GasStack input, GasStack output)
	{
		super(new GasInput(input), new GasOutput(output));
	}
}
