package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToInfuseTypeRecipeCategory extends ItemStackToChemicalRecipeCategory<ItemStackToInfuseTypeRecipe> {

    public ItemStackToInfuseTypeRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToInfuseTypeRecipe> recipeType) {
        super(helper, recipeType, MekanismJEI.TYPE_INFUSION, true);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}