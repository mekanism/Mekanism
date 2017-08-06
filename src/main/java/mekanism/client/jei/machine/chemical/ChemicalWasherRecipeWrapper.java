package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeWrapper extends BaseRecipeWrapper
{
	public WasherRecipe recipe;
	
	public ChemicalWasherRecipeCategory category;
	
	public ChemicalWasherRecipeWrapper(WasherRecipe r, ChemicalWasherRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(FluidStack.class, new FluidStack(FluidRegistry.WATER, 1000));
		ingredients.setInput(GasStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
	
	@Override
	public ChemicalWasherRecipeCategory getCategory()
	{
		return category;
	}
}
