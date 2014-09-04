package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;

public class GasOutput extends MachineOutput
{
	public GasStack output;

	public GasOutput(GasStack stack)
	{
		output = stack;
	}
}
