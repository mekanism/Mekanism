package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class PRCRecipeHandler implements IRecipeHandler<PRCRecipeWrapper>
{
	private final PRCRecipeCategory category;

	public PRCRecipeHandler(PRCRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return PRCRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull PRCRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull PRCRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
}
