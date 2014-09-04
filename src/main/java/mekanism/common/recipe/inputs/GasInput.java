package mekanism.common.recipe.inputs;

import mekanism.api.gas.GasStack;

public class GasInput extends MachineInput
{
	public GasStack ingredient;

	public GasInput(GasStack stack)
	{
		ingredient = stack;
	}

	@Override
	public int hashIngredients()
	{
		return ingredient.hashCode();
	}

	@Override
	public boolean testEquality(MachineInput other)
	{
		return other instanceof GasInput && ((GasInput)other).ingredient.hashCode() == ingredient.hashCode();
	}
}
