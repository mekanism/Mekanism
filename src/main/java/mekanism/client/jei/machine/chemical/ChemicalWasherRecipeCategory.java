package mekanism.client.jei.machine.chemical;

import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.WasherRecipe;
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

import java.util.List;

public class ChemicalWasherRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	public IDrawable fluidOverlay;
	
	public WasherRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public ChemicalWasherRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiChemicalWasher.png", "chemical_washer", "tile.MachineBlock2.ChemicalWasher.name", null);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 3;
		yOffset = 3;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 70);
		fluidOverlay = guiHelper.createDrawable(new ResourceLocation(guiTexture), 176, 4, 16, 59);
	}
	
	@Override
	public void drawExtras(Minecraft minecraft)
	{
		super.drawExtras(minecraft);
		
		drawTexturedRect(61-xOffset, 39-yOffset, 176, 63, 55, 8);
		
		if(tempRecipe.getInput().ingredient != null)
		{
			displayGauge(58, 27-xOffset, 14-yOffset, 176, 4, 58, null, tempRecipe.getInput().ingredient);
		}

		if(tempRecipe.getOutput().output != null)
		{
			displayGauge(58, 134-xOffset, 14-yOffset, 176, 4, 58, null, tempRecipe.getOutput().output);
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
		if(recipeWrapper instanceof ChemicalWasherRecipeWrapper)
		{
			tempRecipe = ((ChemicalWasherRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, true, 6-xOffset, 5-yOffset, 16, 58, 1000, false, fluidOverlay);
		fluidStacks.set(0, ingredients.getInputs(FluidStack.class).get(0));
		fluidStacks.addTooltipCallback(new ITooltipCallback<FluidStack>() {

			@Override
			public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip)
			{
				tooltip.remove(1);
			}
		});
	}
}
