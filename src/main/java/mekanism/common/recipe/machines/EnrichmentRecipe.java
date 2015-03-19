package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class EnrichmentRecipe extends BasicMachineRecipe<EnrichmentRecipe>
{
	public EnrichmentRecipe(ItemStackInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public EnrichmentRecipe(ItemStack input, ItemStack output)
	{
		super(input, output);
	}

	@Override
	public EnrichmentRecipe copy()
	{
		return new EnrichmentRecipe(getInput().copy(), getOutput().copy());
	}
}
