package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.outputs.GasOutput;

public class ChemicalInfuserRecipe extends MachineRecipe<ChemicalPairInput, GasOutput, ChemicalInfuserRecipe>
{
	public ChemicalInfuserRecipe(ChemicalPairInput input, GasOutput output)
	{
		super(input, output);
	}

	public ChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output)
	{
		this(new ChemicalPairInput(leftInput, rightInput), new GasOutput(output));
	}

	public ChemicalInfuserRecipe copy()
	{
		return new ChemicalInfuserRecipe(getInput().copy(), getOutput().copy());
	}

	public boolean canOperate(GasTank leftTank, GasTank rightTank, GasTank outputTank)
	{
		return getInput().useGas(leftTank, rightTank, false) && getOutput().applyOutputs(outputTank, false, 1);
	}

	public void operate(GasTank leftInput, GasTank rightInput, GasTank outputTank)
	{
		if(getInput().useGas(leftInput, rightInput, true))
		{
			getOutput().applyOutputs(outputTank, true, 1);
		}
	}
}
