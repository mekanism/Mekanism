package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.GasInput;
import mekanism.common.recipe.outputs.GasOutput;

public class GasCentrifugeRecipe extends MachineRecipe<GasInput, GasOutput, GasCentrifugeRecipe>
{
	public GasCentrifugeRecipe(GasStack input, GasStack output)
	{
		super(new GasInput(input), new GasOutput(output));
	}

	public GasCentrifugeRecipe(GasInput input, GasOutput output)
	{
		super(input, output);
	}

	@Override
	public GasCentrifugeRecipe copy()
	{
		return new GasCentrifugeRecipe(getInput(), getOutput());
	}

	public boolean canOperate(GasTank inputTank, GasTank outputTank)
	{
		return getInput().useGas(inputTank, false) && getOutput().applyOutputs(outputTank, false);
	}

	public void operate(GasTank inputTank, GasTank outputTank)
	{
		if(getInput().useGas(inputTank, true))
		{
			getOutput().applyOutputs(outputTank, true);
		}
	}

}
