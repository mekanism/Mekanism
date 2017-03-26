package mekanism.client.jei.crafting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapedMekanismRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import mezz.jei.util.BrokenCraftingRecipeException;
import mezz.jei.util.ErrorUtil;
import net.minecraft.item.ItemStack;

public class ShapedMekanismRecipeWrapper extends BlankRecipeWrapper implements IShapedCraftingRecipeWrapper
{
	private final IJeiHelpers jeiHelpers;
	private final ShapedMekanismRecipe recipe;
	
	public ShapedMekanismRecipeWrapper(IJeiHelpers helpers, ShapedMekanismRecipe r) 
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
			List<List<ItemStack>> inputs = stackHelper.expandRecipeItemStackInputs(Arrays.asList(recipe.getInput()));
			ingredients.setInputLists(ItemStack.class, inputs);
			ingredients.setOutput(ItemStack.class, recipeOutput);
		} catch (RuntimeException e) {
			String info = ErrorUtil.getInfoFromBrokenCraftingRecipe(recipe, Arrays.asList(recipe.getInput()), recipeOutput);
			throw new BrokenCraftingRecipeException(info, e);
		}
	}

	@Override
	public int getWidth() 
	{
		return recipe.width;
	}

	@Override
	public int getHeight()
	{
		return recipe.height;
	}
}
