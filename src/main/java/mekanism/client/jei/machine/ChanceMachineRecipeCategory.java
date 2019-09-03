package mekanism.client.jei.machine;

import java.util.Arrays;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.outputs.ChanceOutput;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public abstract class ChanceMachineRecipeCategory<RECIPE extends ChanceMachineRecipe<RECIPE>> extends BaseRecipeCategory<RECIPE> {

    public ChanceMachineRecipeCategory(IGuiHelper helper, MekanismBlock mekanismBlock, ProgressBar progress) {
        super(helper, "mekanism:gui/basic_machine.png", mekanismBlock, progress, 28, 16, 144, 54);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 55, 16));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 55, 52).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT_WIDE, this, guiLocation, 111, 30));
        guiElements.add(new GuiVerticalPowerBar(this, () -> 1F, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (double) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 77, 37));
    }

    @Override
    public void setIngredients(RECIPE recipe, IIngredients ingredients) {
        ChanceOutput output = recipe.getOutput();
        ingredients.setInput(VanillaTypes.ITEM, recipe.getInput().ingredient);
        ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(output.primaryOutput, output.secondaryOutput));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RECIPE recipe, IIngredients ingredients) {
        IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
        itemStacks.init(0, true, 27, 0);
        itemStacks.init(1, false, 87, 18);
        itemStacks.init(2, false, 103, 18);
        itemStacks.set(0, recipe.recipeInput.ingredient);
        ChanceOutput output = recipe.getOutput();
        if (output.hasPrimary()) {
            itemStacks.set(1, output.primaryOutput);
        }
        if (output.hasSecondary()) {
            itemStacks.set(2, output.secondaryOutput);
        }
    }

    @Override
    public void draw(RECIPE recipe, double mouseX, double mouseY) {
        super.draw(recipe, mouseX, mouseY);
        ChanceOutput output = recipe.getOutput();
        if (output.hasSecondary()) {
            getFont().drawString(Math.round(output.secondaryChance * 100) + "%", 104, 41, 0x404040);
        }
    }
}