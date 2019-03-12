package mekanism.client.jei.machine.other;

import javax.annotation.Nonnull;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeWrapper implements IRecipeWrapper
{
	public Fluid fluidType;
	public Gas gasType;
	
	public boolean condensentrating = true;
	
	public RotaryCondensentratorRecipeWrapper(Fluid fluid, Gas gas, boolean b)
	{
		fluidType = fluid;
		gasType = gas;
		
		condensentrating = b;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) 
	{
		if(condensentrating)
		{
			ingredients.setInput(MekanismJEI.GAS_INGREDIENT_TYPE, new GasStack(gasType, 1));
			ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(fluidType, 1));
		}
		else {
			ingredients.setInput(VanillaTypes.FLUID, new FluidStack(fluidType, 1));
			ingredients.setOutput(MekanismJEI.GAS_INGREDIENT_TYPE, new GasStack(gasType, 1));
		}
	}
	
	@Override
	public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		minecraft.fontRenderer.drawString(condensentrating ? LangUtils.localize("gui.condensentrating") : LangUtils.localize("gui.decondensentrating"), 6-3, 74-12, 0x404040, false);
	}
}
