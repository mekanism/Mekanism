package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ElectrolyticSeparatorRecipeHandler implements IRecipeHandler<ElectrolyticSeparatorRecipeWrapper>
{
	private final ElectrolyticSeparatorRecipeCategory category;

	public ElectrolyticSeparatorRecipeHandler(ElectrolyticSeparatorRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ElectrolyticSeparatorRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ElectrolyticSeparatorRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ElectrolyticSeparatorRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull ElectrolyticSeparatorRecipeWrapper recipe) 
	{
		return category.getUid();
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return getRecipeCategoryUid(null);
	}
}
