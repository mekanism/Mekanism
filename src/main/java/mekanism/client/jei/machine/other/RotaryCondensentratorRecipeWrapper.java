package mekanism.client.jei.machine.other;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.jei.machine.BaseRecipeWrapper;
import mekanism.common.util.LangUtils;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
		FontRenderer fontRendererObj = minecraft.fontRendererObj;
		fontRendererObj.drawString(condensentrating ? LangUtils.localize("gui.condensentrating") : LangUtils.localize("gui.decondensentrating"), 6-3, 74-12, 0x404040, false);
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
	
	@Override
	public RotaryCondensentratorRecipeCategory getCategory()
	{
		return category;
	}
}
