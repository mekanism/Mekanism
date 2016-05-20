package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.api.gas.GasRegistry;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalOxidizerRecipeWrapper extends BlankRecipeWrapper
{
	public OxidationRecipe recipe;
	
	public ChemicalOxidizerRecipeCategory category;
	
	public ChemicalOxidizerRecipeWrapper(OxidationRecipe r, ChemicalOxidizerRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return Arrays.asList(recipe.getInput().ingredient);
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
		
		if(mouseX >= 134-20 && mouseX <= 150-20 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().output));
		}
		
		return currenttip;
	}
}
