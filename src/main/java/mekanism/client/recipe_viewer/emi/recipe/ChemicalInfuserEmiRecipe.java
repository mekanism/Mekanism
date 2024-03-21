package mekanism.client.recipe_viewer.emi.recipe;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChemicalInfuserEmiRecipe extends ChemicalChemicalToChemicalEmiRecipe<Gas, GasStack, ChemicalInfuserRecipe> {

    public ChemicalInfuserEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<ChemicalInfuserRecipe> recipeHolder) {
        super(category, recipeHolder);
    }

    @Override
    protected GuiChemicalGauge<Gas, GasStack, ?> getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }
}