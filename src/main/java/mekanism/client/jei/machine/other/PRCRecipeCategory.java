package mekanism.client.jei.machine.other;

import mekanism.api.gas.GasStack;
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
import mekanism.client.jei.MekanismJEI;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;

public class PRCRecipeCategory extends BaseRecipeCategory {

    public PRCRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/nei/GuiPRC.png", "pressurized_reaction_chamber",
              "tile.MachineBlock2.PressurizedReactionChamber.short.name", ProgressBar.MEDIUM, 3, 11, 170, 68);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, guiLocation, 53, 34));
        guiElements.add(new GuiSlot(SlotType.POWER, this, guiLocation, 140, 18).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, guiLocation, 115, 34));
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD_YELLOW, this, guiLocation, 5, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.STANDARD_RED, this, guiLocation, 28, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL_BLUE, this, guiLocation, 140, 40));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return (float) timer.getValue() / 20F;
            }
        }, progressBar, this, guiLocation, 75, 37));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients) {
        if (recipeWrapper instanceof PRCRecipeWrapper) {
            PressurizedRecipe tempRecipe = ((PRCRecipeWrapper) recipeWrapper).getRecipe();
            IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
            itemStacks.init(0, true, 53 - xOffset, 34 - yOffset);
            itemStacks.init(1, false, 115 - xOffset, 34 - yOffset);
            itemStacks.set(0, tempRecipe.recipeInput.getSolid());
            itemStacks.set(1, tempRecipe.recipeOutput.getItemOutput());
            IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
            fluidStacks.init(0, true, 3, 0, 16, 58, tempRecipe.getInput().getFluid().amount, false, fluidOverlayLarge);
            fluidStacks.set(0, tempRecipe.recipeInput.getFluid());
            IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
            initGas(gasStacks, 0, true, 29 - xOffset, 11 - yOffset, 16, 58, tempRecipe.recipeInput.getGas(), true);
            initGas(gasStacks, 1, false, 141 - xOffset, 41 - yOffset, 16, 28, tempRecipe.recipeOutput.getGasOutput(),
                  true);
        }
    }
}