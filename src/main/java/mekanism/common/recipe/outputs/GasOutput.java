package mekanism.common.recipe.outputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

public class GasOutput extends MachineOutput<GasOutput>
{
	public GasStack output;

	public GasOutput(GasStack stack)
	{
		output = stack;
	}

	@Override
	public GasOutput copy()
	{
		return new GasOutput(output.copy());
	}

	public boolean applyOutputs(GasTank gasTank, boolean doEmit)
	{
		if(gasTank.canReceive(output.getGas()) && gasTank.getNeeded() >= output.amount)
		{
			gasTank.receive(output.copy(), doEmit);
			return true;
		}
		return false;
	}
}
