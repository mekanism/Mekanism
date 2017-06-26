package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ChemicalCrystallizerRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	
	public CrystallizerRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public ChemicalCrystallizerRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiChemicalCrystallizer.png", "chemical_crystallizer", "tile.MachineBlock2.ChemicalCrystallizer.name", null);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 5;
		yOffset = 3;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 147, 79);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		GasStack gas = tempRecipe.getInput().ingredient;

		float f = (float)timer.getValue() / 20F;
		drawTexturedRect(53-xOffset, 61-yOffset, 176, 63, (int)(48*f), 8);

		if(gas != null)
		{
			displayGauge(58, 6-xOffset, 5-yOffset, 176, 4, 58, null, gas);
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
		if(recipeWrapper instanceof ChemicalCrystallizerRecipeWrapper)
		{
			tempRecipe = ((ChemicalCrystallizerRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, false, 130-xOffset, 56-yOffset);

		itemStacks.set(0, tempRecipe.getOutput().output);
	}
}
