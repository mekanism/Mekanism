package mekanism.client.jei.machine.other;

import java.util.Arrays;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;

public class ElectrolyticSeparatorRecipeWrapper implements IRecipeWrapper
{
	private final SeparatorRecipe recipe;
	
	public ElectrolyticSeparatorRecipeWrapper(SeparatorRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(FluidStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutputs(GasStack.class, Arrays.asList(recipe.recipeOutput.leftGas, recipe.recipeOutput.rightGas));
	}

	public SeparatorRecipe getRecipe()
	{
		return recipe;
	}
}
