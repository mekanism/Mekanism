package mekanism.client.jei.machine.chemical;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalInfuserRecipeHandler implements IRecipeHandler<ChemicalInfuserRecipeWrapper>
{
	private final ChemicalInfuserRecipeCategory category;

	public ChemicalInfuserRecipeHandler(ChemicalInfuserRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ChemicalInfuserRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ChemicalInfuserRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ChemicalInfuserRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull ChemicalInfuserRecipeWrapper recipe) 
	{
		return category.getUid();
	}
}
