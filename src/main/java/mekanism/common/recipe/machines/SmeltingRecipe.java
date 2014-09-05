package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class SmeltingRecipe extends BasicMachineRecipe<SmeltingRecipe>
{
	public SmeltingRecipe(ItemStackInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public SmeltingRecipe(ItemStack input, ItemStack output)
	{
		super(input, output);
	}

	@Override
	public SmeltingRecipe copy()
	{
		return new SmeltingRecipe(getInput().copy(), getOutput().copy());
	}
}
