package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class MetallurgicInfuserRecipeWrapper extends BlankRecipeWrapper
{
	public MetallurgicInfuserRecipe recipe;
	
	public MetallurgicInfuserRecipeCategory category;
	
	public MetallurgicInfuserRecipeWrapper(MetallurgicInfuserRecipe r, MetallurgicInfuserRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		List<ItemStack> list = new ArrayList<ItemStack>();
		list.add(recipe.getInput().inputStack);
		list.addAll(MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.type));
		
		return list;
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return Arrays.asList(recipe.getOutput().output);
	}
}
