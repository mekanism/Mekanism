package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class MetallurgicInfuserRecipeHandler implements IRecipeHandler<MetallurgicInfuserRecipeWrapper>
{
	private final MetallurgicInfuserRecipeCategory category;

	public MetallurgicInfuserRecipeHandler(MetallurgicInfuserRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return MetallurgicInfuserRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull MetallurgicInfuserRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull MetallurgicInfuserRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
}
