package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class MachineRecipeWrapper extends BlankRecipeWrapper
{
	public BasicMachineRecipe recipe;
	
	public MachineRecipeCategory category;
	
	public MachineRecipeWrapper(BasicMachineRecipe r, MachineRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return Arrays.asList(((ItemStackInput)recipe.getInput()).ingredient);
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return Arrays.asList(((ItemStackOutput)recipe.getOutput()).output);
	}
}
