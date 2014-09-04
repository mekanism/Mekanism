package mekanism.common.recipe.machines;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.inputs.ChemicalPairInput;
import mekanism.common.recipe.outputs.GasOutput;

public class ChemicalInfuserRecipe extends MachineRecipe<ChemicalPairInput, GasOutput>
{
	public ChemicalInfuserRecipe(GasStack leftInput, GasStack rightInput, GasStack output)
	{
		super(new ChemicalPairInput(leftInput, rightInput), new GasOutput(output));
	}
}
