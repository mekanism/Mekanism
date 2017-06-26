package mekanism.client.jei.machine;

import mekanism.client.jei.BaseRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

import javax.annotation.Nonnull;

public class BaseRecipeHandler<CATEGORY extends BaseRecipeCategory, WRAPPER extends BaseRecipeWrapper> implements IRecipeHandler<WRAPPER>
{
	private final CATEGORY category;
	
	public Class<WRAPPER> wrapperClass;

	public BaseRecipeHandler(CATEGORY c, Class<WRAPPER> wrapper)
	{
		category = c;
		
		wrapperClass = wrapper;
	}

	@Override
	public Class<WRAPPER> getRecipeClass() 
	{
		return wrapperClass;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull WRAPPER recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull WRAPPER recipe) 
	{
		return recipe.getCategory() == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull WRAPPER recipe) 
	{
		return category.getUid();
	}
}
