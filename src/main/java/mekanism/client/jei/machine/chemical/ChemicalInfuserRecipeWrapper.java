package mekanism.client.jei.machine.chemical;

import java.util.Arrays;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeWrapper extends BaseRecipeWrapper
{
	public ChemicalInfuserRecipe recipe;
	
	public ChemicalInfuserRecipeCategory category;
	
	public ChemicalInfuserRecipeWrapper(ChemicalInfuserRecipe r, ChemicalInfuserRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInputs(GasStack.class, Arrays.asList(recipe.recipeInput.leftGas, recipe.recipeInput.rightGas));
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
	
	@Override
	public ChemicalInfuserRecipeCategory getCategory()
	{
		return category;
	}
}
