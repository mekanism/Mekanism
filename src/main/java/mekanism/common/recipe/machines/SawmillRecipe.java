package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.outputs.ChanceOutput;

import net.minecraft.item.ItemStack;

public class SawmillRecipe extends ChanceMachineRecipe<SawmillRecipe>
{
	public SawmillRecipe(ItemStackInput input, ChanceOutput output)
	{
		super(input, output);
	}

	public SawmillRecipe(ItemStack input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance)
	{
		this(new ItemStackInput(input), new ChanceOutput(primaryOutput, secondaryOutput, chance));
	}

	public SawmillRecipe(ItemStack input, ItemStack primaryOutput)
	{
		this(new ItemStackInput(input), new ChanceOutput(primaryOutput));
	}

	@Override
	public SawmillRecipe copy()
	{
		return new SawmillRecipe(getInput().copy(), getOutput().copy());
	}
}
