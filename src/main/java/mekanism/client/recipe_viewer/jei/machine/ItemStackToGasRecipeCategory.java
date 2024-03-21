package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToGasRecipeCategory extends ItemStackToChemicalRecipeCategory<Gas, GasStack, ItemStackToGasRecipe> {

    public ItemStackToGasRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToGasRecipe> recipeType, boolean isConversion) {
        super(helper, recipeType, MekanismJEI.TYPE_GAS, isConversion);
    }

    @Override
    protected GuiGasGauge getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }
}