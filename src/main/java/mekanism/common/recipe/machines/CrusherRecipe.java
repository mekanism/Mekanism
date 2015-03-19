package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class CrusherRecipe extends BasicMachineRecipe<CrusherRecipe>
{
	public CrusherRecipe(ItemStackInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public CrusherRecipe(ItemStack input, ItemStack output)
	{
		super(input, output);
	}

	@Override
	public CrusherRecipe copy()
	{
		return new CrusherRecipe(getInput().copy(), getOutput().copy());
	}
}
