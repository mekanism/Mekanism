package mekanism.client.recipe_viewer.jei.machine;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiInfusionGauge;
import mekanism.client.recipe_viewer.jei.MekanismJEI;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.helpers.IGuiHelper;

public class ItemStackToInfuseTypeRecipeCategory extends ItemStackToChemicalRecipeCategory<InfuseType, InfusionStack, ItemStackToInfuseTypeRecipe> {

    public ItemStackToInfuseTypeRecipeCategory(IGuiHelper helper, IRecipeViewerRecipeType<ItemStackToInfuseTypeRecipe> recipeType) {
        super(helper, recipeType, MekanismJEI.TYPE_INFUSION, true);
    }

    @Override
    protected GuiInfusionGauge getGauge(GaugeType type, int x, int y) {
        return GuiInfusionGauge.getDummy(type, this, x, y);
    }
}