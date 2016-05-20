package mekanism.client.jei.machine.other;

import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class SolarNeutronRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	
	public SolarNeutronRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public SolarNeutronRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiSolarNeutronActivator.png", "solar_neutron_activator", "tile.MachineBlock3.SolarNeutronActivator.name", null);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 3;
		yOffset = 12;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 70);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		drawTexturedRect(64-xOffset, 39-yOffset, 176, 58, 55, 8);

		if(tempRecipe.getInput().ingredient != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 0, 58, null, tempRecipe.getInput().ingredient);
		}

		if(tempRecipe.getOutput().output != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 0, 58, null, tempRecipe.getOutput().output);
		}
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper) 
	{
		if(recipeWrapper instanceof SolarNeutronRecipeWrapper)
		{
			tempRecipe = ((SolarNeutronRecipeWrapper)recipeWrapper).recipe;
		}
	}
}
