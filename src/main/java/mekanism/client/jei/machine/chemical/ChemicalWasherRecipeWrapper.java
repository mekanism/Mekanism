package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.common.recipe.machines.WasherRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeWrapper implements IRecipeWrapper
{
	private final WasherRecipe recipe;
	
	public ChemicalWasherRecipeWrapper(WasherRecipe r)
	{
		recipe = r;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(FluidStack.class, new FluidStack(FluidRegistry.WATER, 1000));
		ingredients.setInput(GasStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}

	public WasherRecipe getRecipe()
	{
		return recipe;
	}
}
