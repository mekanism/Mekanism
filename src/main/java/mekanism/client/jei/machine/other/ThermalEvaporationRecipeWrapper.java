package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeWrapper extends BlankRecipeWrapper
{
	public ThermalEvaporationRecipe recipe;
	
	public ThermalEvaporationRecipeCategory category;
	
	public ThermalEvaporationRecipeWrapper(ThermalEvaporationRecipe r, ThermalEvaporationRecipeCategory c)
	{
		recipe = r;
		category = c;
	}
	
	@Nonnull
	@Override
	public List<FluidStack> getFluidInputs() 
	{
		return Arrays.asList(recipe.getInput().ingredient);
	}
	
	@Nonnull
	@Override
	public List<FluidStack> getFluidOutputs()
	{
		return Arrays.asList(recipe.getOutput().output);
	}
}
