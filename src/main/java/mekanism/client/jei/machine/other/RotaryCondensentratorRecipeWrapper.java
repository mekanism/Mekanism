package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeWrapper extends BaseRecipeWrapper
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
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		if(condensentrating)
		{
			ingredients.setInput(GasStack.class, new GasStack(gasType, 1000));
			ingredients.setOutput(FluidStack.class, new FluidStack(fluidType, 1000));
		}
		else {
			ingredients.setInput(FluidStack.class, new FluidStack(fluidType, 1000));
			ingredients.setOutput(GasStack.class, new GasStack(gasType, 1000));
		}
	}
	
	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		minecraft.fontRenderer.drawString(condensentrating ? LangUtils.localize("gui.condensentrating") : LangUtils.localize("gui.decondensentrating"), 6-3, 74-12, 0x404040, false);
	}
	
	@Override
	public RotaryCondensentratorRecipeCategory getCategory()
	{
		return category;
	}
}
