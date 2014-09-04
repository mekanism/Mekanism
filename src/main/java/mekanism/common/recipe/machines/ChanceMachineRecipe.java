package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ChanceOutput;

public class ChanceMachineRecipe extends MachineRecipe<ItemStackInput, ChanceOutput>
{
	public ChanceMachineRecipe(ItemStackInput input, ChanceOutput output)
	{
		super(input, output);
	}
}
