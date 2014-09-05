package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;

import net.minecraft.item.ItemStack;

public class InjectionRecipe extends AdvancedMachineRecipe<InjectionRecipe>
{
	public InjectionRecipe(AdvancedMachineInput input, ItemStackOutput output)
	{
		super(input, output);
	}

	public InjectionRecipe(ItemStack input, String gasName, ItemStack output)
	{
		super(input, gasName, output);
	}

	@Override
	public InjectionRecipe copy()
	{
		return new InjectionRecipe(getInput().copy(), getOutput().copy());
	}
}
