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
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class MachineRecipeCategory extends BaseRecipeCategory {

    public MachineRecipeCategory(IGuiHelper helper, String name, String unlocalized, ProgressBar progress) {
        super(helper, "mekanism:gui/GuiBasicMachine.png", name, unlocalized, progress, 28, 16, 144, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 55, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 55, 52).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT_LARGE, this, guiLocation, 111, 30));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 77, 37));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof MachineRecipeWrapper) {
            IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
            itemStacks.init(0, true, 27, 0);
            itemStacks.init(1, false, 87, 18);
            itemStacks.set(ingredients);
        }
    }
}