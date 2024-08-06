package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToGasRecipeCategory extends ItemStackToChemicalRecipeCategory<ItemStackToGasRecipe> {

    public ItemStackToGasRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToGasRecipe> recipeType, boolean isConversion) {
        super(helper, recipeType, MekanismJEI.TYPE_GAS, isConversion);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}