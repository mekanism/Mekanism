package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class SolarNeutronRecipeHandler implements IRecipeHandler<SolarNeutronRecipeWrapper>
{
	private final SolarNeutronRecipeCategory category;

	public SolarNeutronRecipeHandler(SolarNeutronRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return SolarNeutronRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull SolarNeutronRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull SolarNeutronRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
}
