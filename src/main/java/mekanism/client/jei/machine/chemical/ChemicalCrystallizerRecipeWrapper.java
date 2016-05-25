package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class ChemicalCrystallizerRecipeWrapper extends BlankRecipeWrapper
{
	public CrystallizerRecipe recipe;
	
	public ChemicalCrystallizerRecipeCategory category;
	
	public ChemicalCrystallizerRecipeWrapper(CrystallizerRecipe r, ChemicalCrystallizerRecipeCategory c)
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
		return Arrays.asList(recipe.getOutput().output);
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 1 && mouseX <= 17 && mouseY >= 5-3 && mouseY <= 63-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().ingredient));
		}
		
		return currenttip;
	}
}
