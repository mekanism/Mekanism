package mekanism.client.jei.machine.chemical;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalDissolutionChamberRecipeHandler implements IRecipeHandler<ChemicalDissolutionChamberRecipeWrapper>
{
	private final ChemicalDissolutionChamberRecipeCategory category;

	public ChemicalDissolutionChamberRecipeHandler(ChemicalDissolutionChamberRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ChemicalDissolutionChamberRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ChemicalDissolutionChamberRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ChemicalDissolutionChamberRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull ChemicalDissolutionChamberRecipeWrapper recipe) 
	{
		return category.getUid();
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return getRecipeCategoryUid(null);
	}
}
