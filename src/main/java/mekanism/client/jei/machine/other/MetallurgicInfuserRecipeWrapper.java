package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.List;

import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
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
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		List<ItemStack> inputStacks = Arrays.asList(recipe.recipeInput.inputStack);
		List<ItemStack> infuseStacks = MetallurgicInfuserRecipeCategory.getInfuseStacks(recipe.getInput().infuse.type);
		
		ingredients.setInput(ItemStack.class, recipe.recipeInput.inputStack);
		ingredients.setInputLists(ItemStack.class, Arrays.asList(inputStacks, infuseStacks));
		ingredients.setOutput(ItemStack.class, recipe.recipeOutput.output);
	}
}
