package mekanism.client.jei.machine.other;

import java.util.Arrays;
import mekanism.api.gas.GasStack;
import mekanism.client.gui.element.GuiPowerBar;
import mekanism.client.gui.element.GuiPowerBar.IPowerInfoHandler;
import mekanism.client.gui.element.GuiProgress;
import mekanism.client.gui.element.GuiProgress.IProgressInfoHandler;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotOverlay;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ElectrolyticSeparatorRecipeCategory extends BaseRecipeCategory<SeparatorRecipe> {

    public ElectrolyticSeparatorRecipeCategory(IGuiHelper helper) {
        super(helper, "mekanism:gui/GuiElectrolyticSeparator.png", MekanismBlock.ELECTROLYTIC_SEPARATOR, ProgressBar.BI, 4, 9, 167, 62);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiFluidGauge.getDummy(GuiGauge.Type.STANDARD, this, guiLocation, 5, 10));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, guiLocation, 58, 18));
        guiElements.add(GuiGasGauge.getDummy(GuiGauge.Type.SMALL, this, guiLocation, 100, 18));
        guiElements.add(new GuiPowerBar(this, new IPowerInfoHandler() {
            @Override
            public double getLevel() {
                return 1F;
            }
        }, guiLocation, 164, 15));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 25, 34));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 58, 51));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 100, 51));
        guiElements.add(new GuiSlot(SlotType.NORMAL, this, guiLocation, 142, 34).with(SlotOverlay.POWER));
        guiElements.add(new GuiProgress(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return 1;
            }
        }, progressBar, this, guiLocation, 78, 29));
    }

    @Override
    public Class<? extends SeparatorRecipe> getRecipeClass() {
        return SeparatorRecipe.class;
    }

    @Override
    public void setIngredients(SeparatorRecipe recipe, IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, recipe.recipeInput.ingredient);
        ingredients.setOutputs(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.recipeOutput.leftGas, recipe.recipeOutput.rightGas));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SeparatorRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        fluidStacks.init(0, true, 2, 2, 16, 58, recipe.getInput().ingredient.amount, false, fluidOverlayLarge);
        fluidStacks.set(0, ingredients.getInputs(VanillaTypes.FLUID).get(0));
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initGas(gasStacks, 0, false, 59 - xOffset, 19 - yOffset, 16, 28, recipe.recipeOutput.leftGas, true);
        initGas(gasStacks, 1, false, 101 - xOffset, 19 - yOffset, 16, 28, recipe.recipeOutput.rightGas, true);
    }
}