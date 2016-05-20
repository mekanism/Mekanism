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
import net.minecraftforge.fluids.FluidRegistry;
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
		
		if(mouseX >= 26-3 && mouseX <= 42-3 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(LangUtils.localizeGasStack(((CachedIORecipe)arecipes.get(recipe)).gasStack));
		}
		else if(mouseX >= 134-3 && mouseX <= 150-3 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(LangUtils.localizeFluidStack(((CachedIORecipe)arecipes.get(recipe)).fluidStack));
		}
		
		return currenttip;
	}
}
