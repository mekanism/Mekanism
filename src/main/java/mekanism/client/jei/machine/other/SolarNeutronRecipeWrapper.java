package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.ingredients.IIngredients;

public class SolarNeutronRecipeWrapper extends BaseRecipeWrapper
{
	public SolarNeutronRecipe recipe;
	
	public SolarNeutronRecipeCategory category;
	
	public SolarNeutronRecipeWrapper(SolarNeutronRecipe r, SolarNeutronRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(GasStack.class, recipe.getInput().ingredient);
		ingredients.setOutput(GasStack.class, recipe.getOutput().output);
	}
	
	@Override
	public SolarNeutronRecipeCategory getCategory()
	{
		return category;
	}
}
