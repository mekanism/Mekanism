package mekanism.client.jei.machine.other;

import java.util.Arrays;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidStack;

public class ElectrolyticSeparatorRecipeWrapper extends BaseRecipeWrapper
{
	public SeparatorRecipe recipe;
	
	public ElectrolyticSeparatorRecipeCategory category;
	
	public ElectrolyticSeparatorRecipeWrapper(SeparatorRecipe r, ElectrolyticSeparatorRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(FluidStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutputs(GasStack.class, Arrays.asList(recipe.recipeOutput.leftGas, recipe.recipeOutput.rightGas));
	}
	
	@Override
	public ElectrolyticSeparatorRecipeCategory getCategory()
	{
		return category;
	}
}
