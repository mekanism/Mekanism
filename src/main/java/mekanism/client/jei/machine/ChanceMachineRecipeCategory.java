package mekanism.client.jei.machine;

import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class ChanceMachineRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	@Nullable
	private ChanceMachineRecipe tempRecipe;
	
	public ChanceMachineRecipeCategory(IGuiHelper helper, String name, String unlocalized, ProgressBar progress)
	{
		super(helper, "mekanism:gui/GuiBasicMachine.png", name, unlocalized, progress);

		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), 28, 16, 144, 54);
	}
	
	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 16));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 52).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_WIDE, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 111, 30));

		guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
			@Override
			public double getLevel()
			{
				return 1F;
			}
		}, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 164, 15));
		guiElements.add(new GuiProgress(new IProgressInfoHandler()
		{
			@Override
			public double getProgress()
			{
				return (double)timer.getValue() / 20F;
			}
		}, progressBar, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 77, 37));
	}

	@Override
	public IDrawable getBackground()
	{
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
	{
		if(!(recipeWrapper instanceof ChanceMachineRecipeWrapper))
		{
			return;
		}

		tempRecipe = ((ChanceMachineRecipeWrapper)recipeWrapper).getRecipe();
		
		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
		
		itemStacks.init(0, true, 27, 0);
		itemStacks.init(1, false, 87, 18);
		itemStacks.init(2, false, 103, 18);

		itemStacks.set(0, ((ItemStackInput)tempRecipe.recipeInput).ingredient);
		
		ChanceOutput output = (ChanceOutput)tempRecipe.getOutput();
		
		if(output.hasPrimary())
		{
			itemStacks.set(1, output.primaryOutput);
		}
		
		if(output.hasSecondary())
		{
			itemStacks.set(2, output.secondaryOutput);
		}
	}
}
