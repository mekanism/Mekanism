package mekanism.client.jei.machine;

import javax.annotation.Nonnull;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChanceMachineRecipeHandler<T extends ChanceMachineRecipeWrapper> implements IRecipeHandler<T>
{
	private final ChanceMachineRecipeCategory category;
	
	public Class<T> wrapperClass;

	public ChanceMachineRecipeHandler(ChanceMachineRecipeCategory c, Class<T> wrapper)
	{
		category = c;
		
		wrapperClass = wrapper;
	}

	@Override
	public Class<T> getRecipeClass() 
	{
		return wrapperClass;
	}

	@Nonnull
	@Override
	public String getRecipeCategoryUid() 
	{
		return category.getUid();
	}

	@Nonnull
	@Override
	public IRecipeWrapper getRecipeWrapper(@Nonnull T recipe)
	{
		return recipe;
	}

	@Override
	public boolean isRecipeValid(@Nonnull T recipe) 
	{
		return recipe.category == category;
	}
	
	@Override
	public String getRecipeCategoryUid(@Nonnull T recipe) 
	{
		return category.getUid();
	}
}
