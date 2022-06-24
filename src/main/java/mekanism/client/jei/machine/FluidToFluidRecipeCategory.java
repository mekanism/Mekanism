package mekanism.client.jei.machine;

import java.util.List;
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
import mekanism.client.jei.MekanismJEIRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import org.jetbrains.annotations.NotNull;

public class FluidToFluidRecipeCategory extends BaseRecipeCategory<FluidToFluidRecipe> {

    private final GuiGauge<?> input;
    private final GuiGauge<?> output;

    public FluidToFluidRecipeCategory(IGuiHelper helper, MekanismJEIRecipeType<FluidToFluidRecipe> recipeType) {
        super(helper, recipeType, MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, 3, 12, 170, 62);
        addElement(new GuiInnerScreen(this, 48, 19, 80, 40, () -> List.of(
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, FluidToFluidRecipe recipe, @NotNull IFocusGroup focusGroup) {
        initFluid(builder, RecipeIngredientRole.INPUT, input, recipe.getInput().getRepresentations());
        initFluid(builder, RecipeIngredientRole.OUTPUT, output, recipe.getOutputDefinition());
    }
}