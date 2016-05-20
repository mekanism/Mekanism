package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class AdvancedMachineRecipeWrapper extends BlankRecipeWrapper
{
	public AdvancedMachineRecipe recipe;
	
	public AdvancedMachineRecipeCategory category;
	
	public AdvancedMachineRecipeWrapper(AdvancedMachineRecipe r, AdvancedMachineRecipeCategory c)
	{
		recipe = r;
		category = c;
	}

	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return Arrays.asList(((AdvancedMachineInput)recipe.getInput()).itemStack);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return Arrays.asList(((ItemStackOutput)recipe.getOutput()).output);
	}
}
