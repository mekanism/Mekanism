package mekanism.client.jei.machine.chemical;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalWasherRecipeHandler implements IRecipeHandler<ChemicalWasherRecipeWrapper>
{
	private final ChemicalWasherRecipeCategory category;

	public ChemicalWasherRecipeHandler(ChemicalWasherRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ChemicalWasherRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ChemicalWasherRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ChemicalWasherRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
}
