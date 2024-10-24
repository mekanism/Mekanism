package mekanism.client.recipe_viewer.emi.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import mekanism.api.heat.HeatAPI;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.recipe_viewer.emi.MekanismEmiRecipeCategory;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.world.item.crafting.RecipeHolder;

public class FluidToFluidEmiRecipe extends MekanismEmiHolderRecipe<FluidToFluidRecipe> {

    public FluidToFluidEmiRecipe(MekanismEmiRecipeCategory category, RecipeHolder<FluidToFluidRecipe> recipeHolder) {
        super(category, recipeHolder);
        addInputDefinition(recipe.getInput());
        addFluidOutputDefinition(recipe.getOutputDefinition());
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        //Note: All these elements except for the inputs are in slightly different x positions than in the normal GUI so that they fit properly in emi
        addElement(widgetHolder, new GuiInnerScreen(this, 48, 19, 86, 40, () -> List.of(
              MekanismLang.MULTIBLOCK_FORMED.translate(), MekanismLang.EVAPORATION_HEIGHT.translate(EvaporationMultiblockData.MAX_HEIGHT),
              MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(HeatAPI.AMBIENT_TEMP, TemperatureUnit.KELVIN, true)),
              MekanismLang.FLUID_PRODUCTION.translate(0.0))
        ).padding(3).clearSpacing());
        addElement(widgetHolder, new GuiDownArrow(this, 32, 39));
        addElement(widgetHolder, new GuiDownArrow(this, 142, 39));
        addElement(widgetHolder, new GuiHorizontalRateBar(this, RecipeViewerUtils.FULL_BAR, 51, 63));
        addSlot(widgetHolder, SlotType.INPUT, 28, 20);
        addSlot(widgetHolder, SlotType.OUTPUT, 28, 51);
        addSlot(widgetHolder, SlotType.INPUT, 138, 20);
        addSlot(widgetHolder, SlotType.OUTPUT, 138, 51);
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 6, 13), input(0));
        initTank(widgetHolder, GuiFluidGauge.getDummy(GaugeType.STANDARD, this, 158, 13), output(0)).recipeContext(this);
    }
}