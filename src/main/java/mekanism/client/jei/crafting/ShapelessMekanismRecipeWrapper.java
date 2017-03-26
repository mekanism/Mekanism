package mekanism.client.jei.crafting;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.util.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class ShapelessMekanismRecipeWrapper extends BlankRecipeWrapper implements ICraftingRecipeWrapper
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

		try {
			List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(recipe.getInput());
			ingredients.setInputLists(ItemStack.class, inputs);

			if (recipeOutput != null) 
			{
				ingredients.setOutput(ItemStack.class, recipeOutput);
			}
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, recipe.getInput(), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}
}
