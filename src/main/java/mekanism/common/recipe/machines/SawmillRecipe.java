package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ChanceOutput;

import net.minecraft.item.ItemStack;

public class SawmillRecipe extends ChanceMachineRecipe
{
	public SawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance)
	{
		super(new ItemStackInput(input), new ChanceOutput(primaryOutput, secondaryOutput, chance));
	}

	public SawmillRecipe(ItemStack input, ItemStack primaryOutput)
	{
		super(new ItemStackInput(input), new ChanceOutput(primaryOutput));
	}
}
