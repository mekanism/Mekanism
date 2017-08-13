package mekanism.client.jei.machine.chemical;

import java.util.Arrays;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ChemicalInfuserRecipeWrapper implements IRecipeWrapper
{
	private final ChemicalInfuserRecipe recipe;
	
	public ChemicalInfuserRecipeWrapper(ChemicalInfuserRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInputs(GasStack.class, Arrays.asList(recipe.recipeInput.leftGas, recipe.recipeInput.rightGas));
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}

	public ChemicalInfuserRecipe getRecipe()
	{
		return recipe;
	}
}
