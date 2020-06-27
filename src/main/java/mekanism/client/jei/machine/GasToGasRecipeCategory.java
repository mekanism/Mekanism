package mekanism.client.jei.machine;

import java.util.Collections;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.client.jei.MekanismJEI;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class GasToGasRecipeCategory extends BaseRecipeCategory<GasToGasRecipe> {

    public GasToGasRecipeCategory(IGuiHelper helper, IBlockProvider mekanismBlock) {
        super(helper, mekanismBlock, 3, 12, 170, 70);
    }

    @Override
    protected void addGuiElements() {
        guiElements.add(new GuiSlot(SlotType.INPUT, this, 4, 55).with(SlotOverlay.MINUS));
        guiElements.add(new GuiSlot(SlotType.OUTPUT, this, 154, 55).with(SlotOverlay.PLUS));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 25, 13));
        guiElements.add(GuiGasGauge.getDummy(GaugeType.STANDARD, this, 133, 13));
        guiElements.add(new GuiProgress(() -> 1, ProgressType.LARGE_RIGHT, this, 64, 39));
    }

    @Override
    public Class<? extends GasToGasRecipe> getRecipeClass() {
        return GasToGasRecipe.class;
    }

    @Override
    public void setIngredients(GasToGasRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, recipe.getOutputRepresentation());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, GasToGasRecipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<GasStack> gasStacks = recipeLayout.getIngredientsGroup(MekanismJEI.TYPE_GAS);
        initChemical(gasStacks, 0, true, 26 - xOffset, 14 - yOffset, 16, 58, recipe.getInput().getRepresentations(), true);
        initChemical(gasStacks, 1, false, 134 - xOffset, 14 - yOffset, 16, 58, Collections.singletonList(recipe.getOutputRepresentation()), true);
    }
}