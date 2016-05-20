package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.api.gas.Gas;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeWrapper extends BlankRecipeWrapper
{
	public Fluid fluidType;
	public Gas gasType;
	
	public boolean condensentrating = true;
	
	public RotaryCondensentratorRecipeCategory category;
	
	public RotaryCondensentratorRecipeWrapper(Fluid fluid, Gas gas, boolean b, RotaryCondensentratorRecipeCategory c)
	{
		fluidType = fluid;
		gasType = gas;
		
		condensentrating = b;
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
		if(condensentrating)
		{
			return new ArrayList<FluidStack>();
		}
		else {
			return Arrays.asList(new FluidStack(fluidType, 1000));
		}
	}

	@Nonnull
	@Override
	public List<ItemStack> getOutputs()
	{
		return new ArrayList<ItemStack>();
	}
	
	@Nonnull
	@Override
	public List<FluidStack> getFluidOutputs() 
	{
		if(condensentrating)
		{
			return Arrays.asList(new FluidStack(fluidType, 1000));
		}
		else {
			return new ArrayList<FluidStack>();
		}
	}
	
	@Nullable
	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY)
	{
		List<String> currenttip = new ArrayList<String>();
		
		if(mouseX >= 26-3 && mouseX <= 42-3 && mouseY >= 14-12 && mouseY <= 72-12)
		{
			currenttip.add(gasType.getLocalizedName());
		}
		
		return currenttip;
	}
}
