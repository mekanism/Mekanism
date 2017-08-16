package mekanism.client.jei.machine.other;

import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class MetallurgicInfuserRecipeWrapper implements IRecipeWrapper
{
	private final MetallurgicInfuserRecipe recipe;
	
	public MetallurgicInfuserRecipeWrapper(MetallurgicInfuserRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients)
	{
		List<ItemStack> inputStacks = Arrays.asList(recipe.recipeInput.inputStack);
		List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.type);
		
		ingredients.setInput(ItemStack.class, recipe.recipeInput.inputStack);
		ingredients.setInputLists(ItemStack.class, Arrays.asList(inputStacks, infuseStacks));
		ingredients.setOutput(ItemStack.class, recipe.recipeOutput.output);
	}

	public MetallurgicInfuserRecipe getRecipe()
	{
		return recipe;
	}
}
