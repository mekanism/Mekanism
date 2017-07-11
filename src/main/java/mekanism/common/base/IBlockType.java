package mekanism.common.base;

import java.util.Collection;

import mekanism.common.recipe.ShapedMekanismRecipe;

public interface IBlockType
{
	public String getBlockName();
	
	public Collection<ShapedMekanismRecipe> getRecipes();
	
	public void addRecipes(Collection<ShapedMekanismRecipe> recipes);
	
	public void addRecipe(ShapedMekanismRecipe recipe);
	
	public boolean isEnabled();
}
