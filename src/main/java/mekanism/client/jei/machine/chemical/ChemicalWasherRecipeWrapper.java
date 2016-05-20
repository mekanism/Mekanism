package mekanism.client.jei.machine.chemical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.LangUtils;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ChemicalWasherRecipeWrapper extends BlankRecipeWrapper
{
	public WasherRecipe recipe;
	
	public ChemicalWasherRecipeCategory category;
	
	public ChemicalWasherRecipeWrapper(WasherRecipe r, ChemicalWasherRecipeCategory c)
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
		return Arrays.asList(new FluidStack(FluidRegistry.WATER, 1000));
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
		
		if(mouseX >= 27-3 && mouseX <= 43-3 && mouseY >= 14-3 && mouseY <= 72-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getInput().ingredient));
		}
		else if(mouseX >= 134-3 && mouseX <= 150-3 && mouseY >= 14-3 && mouseY <= 72-3)
		{
			currenttip.add(LangUtils.localizeGasStack(recipe.getOutput().output));
		}
		
		return currenttip;
	}
}
