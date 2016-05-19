package mekanism.client.jei;

import javax.annotation.Nonnull;

import mekanism.common.recipe.ShapelessMekanismRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

public class ShapelessMekanismRecipeHandler implements IRecipeHandler<ShapelessMekanismRecipe>
{
	@Override
	@Nonnull
	public Class<ShapelessMekanismRecipe> getRecipeClass() 
	{
		return ShapelessMekanismRecipe.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	@Nonnull
	public IRecipeWrapper getRecipeWrapper(@Nonnull ShapelessMekanismRecipe recipe) 
	{
		return new ShapelessMekanismRecipeWrapper(recipe);
	}
	
	@Override
	public boolean isRecipeValid(@Nonnull ShapelessMekanismRecipe recipe) 
	{
		return true;
	}
}
