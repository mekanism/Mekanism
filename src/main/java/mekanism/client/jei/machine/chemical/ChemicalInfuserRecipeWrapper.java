package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalInfuserRecipeWrapper extends BlankRecipeWrapper
{
	public ChemicalInfuserRecipe recipe;
	
	public ChemicalInfuserRecipeCategory category;
	
	public ChemicalInfuserRecipeWrapper(ChemicalInfuserRecipe r, ChemicalInfuserRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return new ArrayList<ItemStack>();
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return new ArrayList<ItemStack>();
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 26-3 && mouseX <= 42-3 && mouseY >= 14-3 && mouseY <= 72-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().leftGas));
		}
		else if(mouseX >= 80-3 && mouseX <= 96-3 && mouseY >= 5-3 && mouseY <= 63-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().output));
		}
		else if(mouseX >= 134-3 && mouseX <= 150-3 && mouseY >= 14-3 && mouseY <= 72-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().rightGas));
		}
		
		return currenttip;
	}
}
