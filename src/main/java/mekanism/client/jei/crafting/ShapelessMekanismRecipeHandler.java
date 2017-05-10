package mekanism.client.jei.crafting;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapelessMekanismRecipeHandler implements IRecipeHandler<ShapelessMekanismRecipe>
{
	private final IJeiHelpers jeiHelpers;
	
	public ShapelessMekanismRecipeHandler(IJeiHelpers helpers) 
	{
		jeiHelpers = helpers;
	}
	
	@Override
	@Nonnull
	public Class<ShapelessMekanismRecipe> getRecipeClass() 
	{
		return ShapelessMekanismRecipe.class;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessMekanismRecipe recipe) 
	{
		return new ShapelessMekanismRecipeWrapper(jeiHelpers, recipe);
	}
	
	@Override
	public boolean isRecipeValid(@Nonnull ShapelessMekanismRecipe recipe) 
	{
		return true;
	}

	@Override
	public String getRecipeCategoryUid(@Nonnull ShapelessMekanismRecipe recipe) 
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return getRecipeCategoryUid(null);
	}
}
