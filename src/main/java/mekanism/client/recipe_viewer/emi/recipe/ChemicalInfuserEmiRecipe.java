package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalInfuserEmiRecipe extends ChemicalChemicalToChemicalEmiRecipe<ChemicalInfuserRecipe> {

    public ChemicalInfuserEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalInfuserRecipe> recipeHolder) {
        super(category, recipeHolder);
    }

    @Override
    protected GuiChemicalGauge getGauge(GaugeType type, int x, int y) {
        return GuiChemicalGauge.getDummy(type, this, x, y);
    }
}