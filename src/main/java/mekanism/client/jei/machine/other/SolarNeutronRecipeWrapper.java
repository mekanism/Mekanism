package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<>();
		
		if(mouseX >= 26-3 && mouseX <= 42-3 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().ingredient));
		}
		else if(mouseX >= 134-3 && mouseX <= 150-3 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().output));
		}
		
		return currenttip;
	}
	
	@Override
	public SolarNeutronRecipeCategory getCategory()
	{
		return category;
	}
}
