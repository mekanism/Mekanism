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
import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.machines.DoubleMachineRecipe;
import mekanism.common.recipe.outputs.ItemStackOutput;
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

public class DoubleMachineRecipeCategory extends BaseRecipeCategory
{
	private final IDrawable background;

	public DoubleMachineRecipeCategory(IGuiHelper helper, String name, String unlocalized, ProgressBar progress)
	{
		super(helper, "mekanism:gui/guibasicmachine.png", name, unlocalized, progress);

		background = guiHelper.createDrawable(new ResourceLocation(guiTexture), 28, 16, 144, 54);
	}

	@Override
	public void addGuiElements()
	{
		guiElements.add(new GuiSlot(SlotType.INPUT, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 16));
		guiElements.add(new GuiSlot(SlotType.POWER, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 30, 34).with(SlotOverlay.POWER));
		guiElements.add(new GuiSlot(SlotType.EXTRA, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 55, 52));
		guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, MekanismUtils.getResource(ResourceType.GUI, stripTexture()), 111, 30));

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
		if(!(recipeWrapper instanceof DoubleMachineRecipeWrapper))
		{
			return;
		}

		DoubleMachineRecipe tempRecipe = ((DoubleMachineRecipeWrapper) recipeWrapper).getRecipe();

		DoubleMachineInput input = (DoubleMachineInput) tempRecipe.recipeInput;

		IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();

		itemStacks.init(0, true, 27, 0);
		itemStacks.init(1, false, 87, 18);
		itemStacks.init(2, false, 27, 36);

		itemStacks.set(0, input.itemStack);
		itemStacks.set(1, ((ItemStackOutput) tempRecipe.recipeOutput).output);
		itemStacks.set(2, input.extraStack);
	}
}
