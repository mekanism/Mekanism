package mekanism.common.recipe.inputs;

import net.minecraftforge.fluids.FluidStack;

public class FluidInput extends MachineInput
{
	public FluidStack ingredient;

	public FluidInput(FluidStack stack)
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
		return other instanceof FluidInput && ingredient.equals(((FluidInput)other).ingredient);
	}
}
