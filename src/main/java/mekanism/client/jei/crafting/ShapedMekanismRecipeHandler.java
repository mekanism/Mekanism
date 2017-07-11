package mekanism.client.jei.crafting;

import mekanism.common.recipe.ShapedMekanismRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapedMekanismRecipeHandler implements IRecipeHandler<ShapedMekanismRecipe>
{
	private final IJeiHelpers jeiHelpers;
	
	public ShapedMekanismRecipeHandler(IJeiHelpers helpers) 
	{
		jeiHelpers = helpers;
	}
	
	@Override
	public Class<ShapedMekanismRecipe> getRecipeClass() 
	{
		return ShapedMekanismRecipe.class;
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ShapedMekanismRecipe recipe)
	{
		return new ShapedMekanismRecipeWrapper(jeiHelpers, recipe);
	}

	@Override
	public boolean isRecipeValid(ShapedMekanismRecipe recipe)
	{
		return true;
	}
	
	@Override
	public String getRecipeCategoryUid(ShapedMekanismRecipe recipe) 
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}
	public String getRecipeCategoryUid()
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}
}
