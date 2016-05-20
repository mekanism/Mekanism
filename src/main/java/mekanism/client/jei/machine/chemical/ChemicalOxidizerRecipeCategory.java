package mekanism.client.jei.machine.chemical;

import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class ChemicalOxidizerRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	
	public OxidationRecipe tempRecipe;
	
	public GuiGasGauge gasOutput;
	
	public ITickTimer timer;
	
	public ChemicalOxidizerRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/GuiChemicalOxidizer.png", "chemical_oxidizer", "tile.MachineBlock2.ChemicalOxidizer.name", ProgressBar.LARGE_RIGHT);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 20;
		yOffset = 12;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 132, 62);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(gasOutput = GuiGasGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiChemicalOxidizer.png"), 133, 13));

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
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);
		
		if(tempRecipe.getOutput().output != null)
		{
			gasOutput.setDummyType(tempRecipe.getOutput().output.getGas());
			gasOutput.renderScale(0, 0, -xOffset, -yOffset);
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
		if(recipeWrapper instanceof ChemicalOxidizerRecipeWrapper)
		{
			tempRecipe = ((ChemicalOxidizerRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, true, 25-xOffset, 35-yOffset);

		itemStacks.set(0, tempRecipe.getInput().ingredient);
	}
}
