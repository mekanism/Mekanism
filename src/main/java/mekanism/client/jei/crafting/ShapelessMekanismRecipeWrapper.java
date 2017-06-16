package mekanism.client.jei.crafting;

import java.util.List;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

public class ShapelessMekanismRecipeWrapper extends BlankRecipeWrapper
{
	private final IJeiHelpers jeiHelpers;
	private final ShapelessMekanismRecipe recipe;

	public ShapelessMekanismRecipeWrapper(IJeiHelpers helpers, ShapelessMekanismRecipe r)
	{
		jeiHelpers = helpers;
		recipe = r;
		
		for(Object input : recipe.getInput()) 
		{
			if(input instanceof ItemStack)
			{
				ItemStack itemStack = (ItemStack)input;
				
				if(itemStack.getCount() != 1) 
				{
					itemStack.setCount(1);
				}
			}
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients)
	{
		IStackHelper stackHelper = jeiHelpers.getStackHelper();
		ItemStack recipeOutput = recipe.getRecipeOutput();

		List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getInput());
		ingredients.setInputLists(ItemStack.class, inputs);

		if(recipeOutput != null && !recipeOutput.isEmpty())
		{
			ingredients.setOutput(ItemStack.class, recipeOutput);
		}
	}
}
