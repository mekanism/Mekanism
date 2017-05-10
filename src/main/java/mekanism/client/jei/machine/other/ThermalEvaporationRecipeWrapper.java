package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeWrapper extends BlankRecipeWrapper
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
}
