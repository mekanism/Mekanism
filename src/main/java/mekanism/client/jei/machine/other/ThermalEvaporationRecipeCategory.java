package mekanism.client.jei.machine.other;

import java.util.List;

import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class ThermalEvaporationRecipeCategory extends BaseRecipeCategory
{
	public IDrawable background;
	
	public ThermalEvaporationRecipe tempRecipe;
	
	public ThermalEvaporationRecipeCategory(IGuiHelper helper)
	{
		super(helper, "mekanism:gui/nei/GuiThermalEvaporationController.png", "thermal_evaporation_plant", "gui.thermalEvaporationController.short", null);

		xOffset = 3;
		yOffset = 12;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 62);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		drawTexturedRect(49-xOffset, 64-yOffset, 176, 59, 78, 8);
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) 
	{
		if(recipeWrapper instanceof ThermalEvaporationRecipeWrapper)
		{
			tempRecipe = ((ThermalEvaporationRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, true, 7-xOffset, 14-yOffset, 16, 58, tempRecipe.getInput().ingredient.amount, false, fluidOverlayLarge);
		fluidStacks.init(1, false, 153-xOffset, 14-yOffset, 16, 58, tempRecipe.getOutput().output.amount, false, fluidOverlayLarge);
		
		fluidStacks.set(0, tempRecipe.recipeInput.ingredient);
		fluidStacks.set(1, tempRecipe.recipeOutput.output);
		fluidStacks.addTooltipCallback((index, input, ingredient, tooltip) -> tooltip.remove(1));
	}
}
