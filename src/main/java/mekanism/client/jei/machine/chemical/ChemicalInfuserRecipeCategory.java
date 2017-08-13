package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	@Nullable
	private ChemicalInfuserRecipe tempRecipe;
	
	public ChemicalInfuserRecipeCategory(IGuiHelper helper)
	{
		super(helper, "mekanism:gui/nei/GuiChemicalInfuser.png", "chemical_infuser", "tile.MachineBlock2.ChemicalInfuser.name", null);

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
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) 
	{
		if(!(recipeWrapper instanceof ChemicalInfuserRecipeWrapper))
		{
			return;
		}

		tempRecipe = ((ChemicalInfuserRecipeWrapper)recipeWrapper).getRecipe();
		
		IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(GasStack.class);
		
		initGas(gasStacks, 0, true, 26-xOffset, 14-yOffset, 16, 58, tempRecipe.getInput().leftGas, true);
		initGas(gasStacks, 1, true, 134-xOffset, 14-yOffset, 16, 58, tempRecipe.getInput().rightGas, true);
		initGas(gasStacks, 2, false, 80-xOffset, 5-yOffset, 16, 58, tempRecipe.getOutput().output, true);
	}
}
