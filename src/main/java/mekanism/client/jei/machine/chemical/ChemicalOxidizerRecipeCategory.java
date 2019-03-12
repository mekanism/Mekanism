package mekanism.client.jei.machine.chemical;

import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ChemicalOxidizerRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	@Nullable
	private OxidationRecipe tempRecipe;
	
	public ChemicalOxidizerRecipeCategory(IGuiHelper helper)
	{
		super(helper, "mekanism:gui/GuiChemicalOxidizer.png", "chemical_oxidizer", "tile.MachineBlock2.ChemicalOxidizer.name", ProgressBar.LARGE_RIGHT);

		xOffset = 20;
		yOffset = 12;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 132, 62);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 133, 13));

		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 25, 35));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return (double)timer.getValue() / 20F;
			}
		}, progressBar, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 62, 39));
	}
	
	@Override
	public IDrawable getBackground() 
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) 
	{
		if(!(recipeWrapper instanceof ChemicalOxidizerRecipeWrapper))
		{
			return;
		}

		tempRecipe = ((ChemicalOxidizerRecipeWrapper)recipeWrapper).getRecipe();
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, true, 25-xOffset, 35-yOffset);
		itemStacks.set(0, tempRecipe.getInput().ingredient);
		
		IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.GAS_INGREDIENT_TYPE);
		
		initGas(gasStacks, 0, false, 134-xOffset, 14-yOffset, 16, 58, tempRecipe.recipeOutput.output, true);
	}
}
