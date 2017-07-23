package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

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
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 59-4 && mouseX <= 75-4 && mouseY >= 19-9 && mouseY <= 47-9)
		{
			currenttip.add(recipe.getOutput().leftGas.getGas().getLocalizedName());
		}
		else if(mouseX >= 101-4 && mouseX <= 117-4 && mouseY >= 19-9 && mouseY <= 47-9)
		{
			currenttip.add(recipe.getOutput().rightGas.getGas().getLocalizedName());
		}
		
		return currenttip;
	}
	
	@Override
	public ElectrolyticSeparatorRecipeCategory getCategory()
	{
		return category;
	}
}
