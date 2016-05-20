package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class SolarNeutronRecipeWrapper extends BlankRecipeWrapper
{
	public SolarNeutronRecipe recipe;
	
	public SolarNeutronRecipeCategory category;
	
	public SolarNeutronRecipeWrapper(SolarNeutronRecipe r, SolarNeutronRecipeCategory c)
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
}
