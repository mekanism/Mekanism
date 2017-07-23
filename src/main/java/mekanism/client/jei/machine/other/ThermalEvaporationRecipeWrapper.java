package mekanism.client.jei.machine.other;

import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeWrapper extends BaseRecipeWrapper
{
	public ThermalEvaporationRecipe recipe;
	
	public ThermalEvaporationRecipeCategory category;
	
	public ThermalEvaporationRecipeWrapper(ThermalEvaporationRecipe r, ThermalEvaporationRecipeCategory c)
	{
		recipe = r;
		category = c;
	}

	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(FluidStack.class, recipe.getInput().ingredient);
		ingredients.setOutput(FluidStack.class, recipe.getOutput().output);
	}
	
	@Override
	public ThermalEvaporationRecipeCategory getCategory()
	{
		return category;
	}
}
