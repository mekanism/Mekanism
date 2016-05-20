package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.SeparatorRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ElectrolyticSeparatorRecipeWrapper extends BlankRecipeWrapper
{
	public SeparatorRecipe recipe;
	
	public ElectrolyticSeparatorRecipeCategory category;
	
	public ElectrolyticSeparatorRecipeWrapper(SeparatorRecipe r, ElectrolyticSeparatorRecipeCategory c)
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
	public List<FluidStack> getFluidInputs() 
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
}
