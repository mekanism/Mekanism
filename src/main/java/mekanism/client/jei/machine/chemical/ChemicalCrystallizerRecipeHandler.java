package mekanism.client.jei.machine.chemical;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalCrystallizerRecipeHandler implements IRecipeHandler<ChemicalCrystallizerRecipeWrapper>
{
	private final ChemicalCrystallizerRecipeCategory category;

	public ChemicalCrystallizerRecipeHandler(ChemicalCrystallizerRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return ChemicalCrystallizerRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull ChemicalCrystallizerRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull ChemicalCrystallizerRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
}
