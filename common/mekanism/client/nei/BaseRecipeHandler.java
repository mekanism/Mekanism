package mekanism.client.nei;

import static codechicken.core.gui.GuiDraw.changeTexture;
import static codechicken.core.gui.GuiDraw.drawTexturedModalRect;
import static codechicken.core.gui.GuiDraw.gui;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraftforge.fluids.FluidStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public abstract class BaseRecipeHandler extends TemplateRecipeHandler
{
	public void displayGauge(int length, int xPos, int yPos, int overlayX, int overlayY, int scale, FluidStack fluid, GasStack gas)
	{
	    if(fluid == null && gas == null)
	    {
	        return;
	    }
	    
		int start = 0;

		while(true)
		{
			int renderRemaining = 0;

			if(scale > 16) 
			{
				renderRemaining = 16;
				scale -= 16;
			} 
			else {
				renderRemaining = scale;
				scale = 0;
			}

			changeTexture(MekanismRenderer.getBlocksTexture());
			
			if(fluid != null)
			{
				gui.drawTexturedModelRectFromIcon(xPos, yPos + length - renderRemaining - start, fluid.getFluid().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			else if(gas != null)
			{
				gui.drawTexturedModelRectFromIcon(xPos, yPos + length - renderRemaining - start, gas.getGas().getIcon(), 16, 16 - (16 - renderRemaining));
			}
			
			start+=16;

			if(renderRemaining == 0 || scale == 0)
			{
				break;
			}
		}

		changeTexture(getGuiTexture());
		drawTexturedModalRect(xPos, yPos, overlayX, overlayY, 16, length+1);
	}
	
	/*
	 * true = usage, false = recipe
	 */
	public void doGasTransfer(int recipe, boolean type)
	{
		
	}
	
	public class ExtraRecipe extends TemplateRecipeHandler.CachedRecipe
	{
		@Override
		public PositionedStack getResult()
		{
			return null;
		}
	}
}
