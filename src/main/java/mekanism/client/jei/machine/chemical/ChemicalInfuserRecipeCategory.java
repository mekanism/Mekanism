package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	
	public ChemicalInfuserRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public ChemicalInfuserRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiChemicalInfuser.png", "chemical_infuser", "tile.MachineBlock2.ChemicalInfuser.name", null);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 3;
		yOffset = 3;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 80);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		drawTexturedRect(47-xOffset, 39-yOffset, 176, 71, 28, 8);
		drawTexturedRect(101-xOffset, 39-yOffset, 176, 63, 28, 8);

		if(tempRecipe.getInput().leftGas != null)
		{
			displayGauge(58, 26-xOffset, 14-yOffset, 176, 4, 58, null, tempRecipe.getInput().leftGas);
		}

		if(tempRecipe.getOutput().output != null)
		{
			displayGauge(58, 80-xOffset, 5-yOffset, 176, 4, 58, null, tempRecipe.getOutput().output);
		}

		if(tempRecipe.getInput().rightGas != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 4, 58, null, tempRecipe.getInput().rightGas);
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
		if(recipeWrapper instanceof ChemicalInfuserRecipeWrapper)
		{
			tempRecipe = ((ChemicalInfuserRecipeWrapper)recipeWrapper).recipe;
		}
	}
}
