package mekanism.client.jei.machine.chemical;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalOxidizerRecipeHandler implements IRecipeHandler<ChemicalOxidizerRecipeWrapper>
{
	private final ChemicalOxidizerRecipeCategory category;

	public ChemicalOxidizerRecipeHandler(ChemicalOxidizerRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ChemicalOxidizerRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ChemicalOxidizerRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ChemicalOxidizerRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull ChemicalOxidizerRecipeWrapper recipe) 
	{
		return category.getUid();
	}
}
