package mekanism.client.jei.machine;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mezz.jei.api.helpers.IGuiHelper;

public class ChemicalInfuserRecipeCategory extends ChemicalChemicalToChemicalRecipeCategory<Gas, GasStack, ChemicalInfuserRecipe> {

    public ChemicalInfuserRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<ChemicalInfuserRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.CHEMICAL_INFUSER, MekanismJEI.TYPE_GAS, 3, 3, 170, 80);
    }

    @Override
    protected GuiChemicalGauge<Gas, GasStack, ?> getGauge(GaugeType type, int x, int y) {
        return GuiGasGauge.getDummy(type, this, x, y);
    }
}