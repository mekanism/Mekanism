package mekanism.client.jei.machine.other;

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
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
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

public class ElectrolyticSeparatorRecipeCategory extends BaseRecipeCategory
{
	public IGuiHelper guiHelper;
	
	public IDrawable background;
	public IDrawable fluidOverlay;
	
	public GuiGasGauge leftGas;
	public GuiGasGauge rightGas;
	
	public SeparatorRecipe tempRecipe;
	
	public ITickTimer timer;
	
	public ElectrolyticSeparatorRecipeCategory(IGuiHelper helper)
	{
		super("mekanism:gui/GuiElectrolyticSeparator.png", "electrolytic_separator", "tile.MachineBlock2.ElectrolyticSeparator.name", ProgressBar.BI);
		
		guiHelper = helper;
		
		timer = helper.createTickTimer(20, 20, false);
		
		xOffset = 4;
		yOffset = 9;
		
		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), xOffset, yOffset, 167, 62);
		fluidOverlay = guiHelper.createDrawable(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, GuiGauge.Type.STANDARD.textureLocation), 19, 1, 16, 59);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 5, 10));
		guiElements.add(leftGas = GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 18));
		guiElements.add(rightGas = GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 18));
		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return 1F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 25, 34));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 58, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 100, 51));
		guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 142, 34).with(SlotOverlay.POWER));

		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return 1;
			}
		}, progressBar, this, MekanismUtils.getResource(ResourceType.GUI, "GuiElectrolyticSeparator.png"), 78, 29));
	}
	
	@Override
	public void drawExtras(Minecraft minecraft)
	{
		super.drawExtras(minecraft);
		
		if(tempRecipe.getOutput().leftGas != null)
		{
			leftGas.setDummyType(tempRecipe.getOutput().leftGas.getGas());
			leftGas.renderScale(0, 0, -xOffset, -yOffset);
		}

		if(tempRecipe.getOutput().rightGas != null)
		{
			rightGas.setDummyType(tempRecipe.getOutput().rightGas.getGas());
			rightGas.renderScale(0, 0, -xOffset, -yOffset);
		}
		
		changeTexture(guiLocation);
	}
	
	@Override
	public IDrawable getBackground()
	{
		return background;
	}
	
	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		if(recipeWrapper instanceof ElectrolyticSeparatorRecipeWrapper)
		{
			tempRecipe = ((ElectrolyticSeparatorRecipeWrapper)recipeWrapper).recipe;
		}
		
		IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
		
		fluidStacks.init(0, true, 2, 2, 16, 58, tempRecipe.getInput().ingredient.amount, false, fluidOverlay);
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
