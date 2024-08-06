package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;

public class ChemicalInfuserRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory<ChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ChemicalInfuserRecipe> recipeType) {
        super(helper, recipeType, MekanismJEI.TYPE_GAS);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}