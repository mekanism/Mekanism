package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalDissolutionChamberRecipeWrapper extends BlankRecipeWrapper
{
	public DissolutionRecipe recipe;
	
	public ChemicalDissolutionChamberRecipeCategory category;
	
	public ChemicalDissolutionChamberRecipeWrapper(DissolutionRecipe r, ChemicalDissolutionChamberRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		ingredients.setInput(GasStack.class, new GasStack(GasRegistry.getGas("sulfuricAcid"), 1000));
		ingredients.setInput(ItemStack.class, recipe.recipeInput.ingredient);
		ingredients.setOutput(GasStack.class, recipe.recipeOutput.output);
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 6-3 && mouseX <= 22-3 && mouseY >= 5-3 && mouseY <= 63-3)
		{
			currenttip.add(GasRegistry.getGas("sulfuricAcid").getLocalizedName());
		}
		else if(mouseX >= 134-3 && mouseX <= 150-3 && mouseY >= 14-3 && mouseY <= 72-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().output));
		}
		
		return currenttip;
	}
}
