package mekanism.client.jei.machine.other;

import java.util.List;

import mekanism.client.gui.element.GuiFluidGauge;
import mekanism.client.gui.element.GuiGasGauge;
import mekanism.client.gui.element.GuiGauge;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class PRCRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	public IDrawable fluidOverlay;
	
	public GuiGasGauge gasInput;
	public GuiGasGauge gasOutput;
	
	public PressurizedRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public PRCRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/nei/GuiPRC.png", "pressurized_reaction_chamber", "tile.MachineBlock2.PressurizedReactionChamber.short.name", ProgressBar.MEDIUM);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 3;
		yOffset = 11;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 170, 68);
		fluidOverlay = guiHelper.createDrawable(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, GuiGauge.Type.STANDARD.textureLocation), 19, 1, 16, 59);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 53, 34));
		guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 140, 18).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 115, 34));
		
		guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD_YELLOW, this, guiLocation, 5, 10));
		guiElements.add(gasInput = GuiGasGauge.getDummy(GuiGauge.Type.STANDARD_RED, this, guiLocation, 28, 10));
		guiElements.add(gasOutput = GuiGasGauge.getDummy(GuiGauge.Type.SMALL_BLUE, this, guiLocation, 140, 40));

		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return 1F;
			}
		}, guiLocation, 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return (float)timer.getValue() / 20F;
			}
		}, progressBar, this, guiLocation, 75, 37));
	}
	
	@Override
	public void drawExtras(Minecraft minecraft) 
	{
		super.drawExtras(minecraft);

		if(tempRecipe.getInput().getGas() != null)
		{
			gasInput.setDummyType(tempRecipe.getInput().getGas().getGas());
			gasInput.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(tempRecipe.getOutput().getGasOutput() != null)
		{
			gasOutput.setDummyType(tempRecipe.getOutput().getGasOutput().getGas());
			gasOutput.renderScale(0, 0, -xOffset, -yOffset);
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
		if(recipeWrapper instanceof PRCRecipeWrapper)
		{
			tempRecipe = ((PRCRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, true, 53-xOffset, 34-yOffset);
		itemStacks.init(1, false, 115-xOffset, 34-yOffset);
		
		itemStacks.set(0, tempRecipe.recipeInput.getSolid());
		itemStacks.set(1, tempRecipe.recipeOutput.getItemOutput());
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, true, 3, 0, 16, 58, tempRecipe.getInput().getFluid().amount, false, fluidOverlay);
		fluidStacks.set(0, tempRecipe.recipeInput.getFluid());
		fluidStacks.addTooltipCallback(new ITooltipCallback<FluidStack>() {
			@Override
			public void onTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) 
			{
				tooltip.remove(1);
			}
		});
	}
}
