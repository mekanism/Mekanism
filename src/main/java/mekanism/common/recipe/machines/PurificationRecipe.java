package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class PurificationRecipe extends AdvancedMachineRecipe<PurificationRecipe>
{
	public PurificationRecipe(AdvancedMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public PurificationRecipe(ItemStack input, ItemStack output)
	{
		super(input, "oxygen", output);
	}

	@Override
	public PurificationRecipe copy()
	{
		return new PurificationRecipe(getInput().copy(), getOutput().copy());
	}
}
