package mekanism.client.jei.machine;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.jei.BaseRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiFluidStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;

public class FluidToFluidRecipeCategory extends BaseRecipeCategory<FluidToFluidRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public FluidToFluidRecipeCategory(IGuiHelper helper) {
        super(helper, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 3, 12, 170, 62);
        addElement(new GuiInnerScreen(this, 48, 19, 80, 40, () -> Arrays.asList(
              MekanismLang.MULTIBLOCK_FORMED.translate(), MekanismLang.EVAPORATION_HEIGHT.translate(EvaporationMultiblockData.MAX_HEIGHT),
              MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              MekanismLang.FLUID_PRODUCTION.translate(0.0))
        ).spacing(1));
        addElement(new GuiDownArrow(this, 32, 39));
        addElement(new GuiDownArrow(this, 136, 39));
        addElement(new GuiHorizontalRateBar(this, FULL_BAR, 48, 63));
        addSlot(SlotType.INPUT, 28, 20);
        addSlot(SlotType.OUTPUT, 28, 51);
        addSlot(SlotType.INPUT, 132, 20);
        addSlot(SlotType.OUTPUT, 132, 51);
        input = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13));
        output = addElement(GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 152, 13));
    }

    @Override
    public Class<? extends FluidToFluidRecipe> getRecipeClass() {
        return FluidToFluidRecipe.class;
    }

    @Override
    public void setIngredients(FluidToFluidRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInput().getRepresentations()));
        ingredients.setOutputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getOutputDefinition()));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, FluidToFluidRecipe recipe, IIngredients ingredients) {
        IGuiFluidStackGroup fluidStacks = recipeLayout.getFluidStacks();
        initFluid(fluidStacks, 0, true, input, recipe.getInput().getRepresentations());
        initFluid(fluidStacks, 1, false, output, recipe.getOutputDefinition());
    }
}