package mekanism.client.jei.machine.other;

import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class RotaryCondensentratorRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	public IDrawable fluidOverlay;
	
	public RotaryCondensentratorRecipeWrapper tempRecipe;
	
	public ITickTimer timer;
	
	public RotaryCondensentratorRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiRotaryCondensentrator.png", "rotary_condensentrator", "nei.rotaryCondensentrator", null);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 3;
		yOffset = 12;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 71);
		fluidOverlay = guiHelper.createDrawable(new ResourceLocation(guiTexture), 176, 40, 16, 59);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		if(tempRecipe.condensentrating)
		{
			drawTexturedRect(64-xOffset, 39-yOffset, 176, 123, 48, 8);
		}
		else {
			drawTexturedRect(64-xOffset, 39-yOffset, 176, 115, 48, 8);
		}

		if(tempRecipe.gasType != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 40, 58, null, new GasStack(tempRecipe.gasType, 1));
		}
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) 
	{
		if(recipeWrapper instanceof RotaryCondensentratorRecipeWrapper)
		{
			tempRecipe = (RotaryCondensentratorRecipeWrapper)recipeWrapper;
		}
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, !tempRecipe.condensentrating, 134-xOffset, 14-yOffset, 16, 58, 1000, false, fluidOverlay);
		
		if(tempRecipe.condensentrating)
		{
			fluidStacks.set(0, ingredients.getOutputs(FluidStack.class).get(0));
		}
		else {
			fluidStacks.set(0, ingredients.getInputs(FluidStack.class).get(0));
		}
	}
}
