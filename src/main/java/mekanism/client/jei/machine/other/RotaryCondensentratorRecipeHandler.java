package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RotaryCondensentratorRecipeHandler implements IRecipeHandler<RotaryCondensentratorRecipeWrapper>
{
	private final RotaryCondensentratorRecipeCategory category;

	public RotaryCondensentratorRecipeHandler(RotaryCondensentratorRecipeCategory c)
	{
		category = c;
	}

	@Override
	public Class getRecipeClass() 
	{
		return RotaryCondensentratorRecipeWrapper.class;
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull RotaryCondensentratorRecipeWrapper recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull RotaryCondensentratorRecipeWrapper recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull RotaryCondensentratorRecipeWrapper recipe) 
	{
		return category.getUid();
	}

	@Override
	public String getRecipeCategoryUid()
	{
		return getRecipeCategoryUid(null);
	}
}
