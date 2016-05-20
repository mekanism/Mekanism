package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class PRCRecipeWrapper extends BlankRecipeWrapper
{
	public PressurizedRecipe recipe;
	
	public PRCRecipeCategory category;
	
	public PRCRecipeWrapper(PressurizedRecipe r, PRCRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getInputs()
	{
		return Arrays.asList(recipe.getInput().getSolid());
	}
	
	@Nonnull
	@Override
	public List<FluidStack> getFluidInputs() 
	{
		return Arrays.asList(recipe.getInput().getFluid());
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return Arrays.asList(recipe.getOutput().getItemOutput());
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 29-3 && mouseX <= 45-3 && mouseY >= 11-12 && mouseY <= 69-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().getGas()));
		}
		else if(mouseX >= 141-3 && mouseX <= 157-3 && mouseY >= 41-12 && mouseY <= 69-12)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().getGasOutput()));
		}
		
		return currenttip;
	}
}
