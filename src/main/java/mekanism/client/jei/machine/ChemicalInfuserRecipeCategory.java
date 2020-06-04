package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class ChemicalInfuserRecipeCategory extends BaseRecipeCategory<ChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.CHEMICAL_INFUSER, 3, 3, 170, 80);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 79, 4));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiSlot(SlotType.POWER, this, 154, 4).with(SlotOverlay.POWER));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 154, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 4, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 79, 64).with(SlotOverlay.PLUS));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.SMALL_RIGHT, this, 47, 39));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.SMALL_LEFT, this, 101, 39));
    }

    @Override
    public Class<? extends ChemicalInfuserRecipe> getRecipeClass() {
        return ChemicalInfuserRecipe.class;
    }

    @Override
    public void setIngredients(ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Arrays.asList(recipe.getLeftInput().getRepresentations(), recipe.getRightInput().getRepresentations()));
        ingredients.setOutputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, ChemicalInfuserRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getLeftInput().getRepresentations(), true);
        initChemical(gasStacks, 1, true, 134 - xOffset, 14 - yOffset, 16, 58, recipe.getRightInput().getRepresentations(), true);
        initChemical(gasStacks, 2, false, 80 - xOffset, 5 - yOffset, 16, 58, recipe.getOutputDefinition(), true);
    }
}