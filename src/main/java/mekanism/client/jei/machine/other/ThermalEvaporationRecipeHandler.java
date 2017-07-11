package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ThermalEvaporationRecipeHandler implements IRecipeHandler<ThermalEvaporationRecipeWrapper>
{
	private final ThermalEvaporationRecipeCategory category;

	public ThermalEvaporationRecipeHandler(ThermalEvaporationRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ThermalEvaporationRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ThermalEvaporationRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ThermalEvaporationRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull ThermalEvaporationRecipeWrapper recipe) 
	{
		return category.getUid();
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return getRecipeCategoryUid(null);
	}
}
