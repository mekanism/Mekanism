package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController, MekanismTileContainer<TileEntityThermalEvaporationController>> {

    private GuiElement inputGauge, outputGauge;

    public GuiThermalEvaporationController(MekanismTileContainer<TileEntityThermalEvaporationController> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth += 20;
        inventoryLabelX += 10;
        inventoryLabelY += 2;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 48, 19, 100, 40, () -> {
            EvaporationMultiblockData multiblock = tile.getMultiblock();
            return List.of(MekanismLang.MULTIBLOCK_FORMED.translate(), MekanismLang.EVAPORATION_HEIGHT.translate(multiblock.height()),
                  MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(multiblock.getTemperature(), TemperatureUnit.KELVIN, true)),
                  MekanismLang.FLUID_PRODUCTION.translate(Math.round(multiblock.lastGain * 100D) / 100D));
        }).padding(3).clearSpacing().recipeViewerCategories(RecipeViewerRecipeType.EVAPORATING));
        addRenderableWidget(new GuiDownArrow(this, 32, 39));
        addRenderableWidget(new GuiDownArrow(this, 156, 39));
        addRenderableWidget(new GuiHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getMultiblock().getTemperature(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getMultiblock().getTemperature() / EvaporationMultiblockData.MAX_MULTIPLIER_TEMP);
            }
        }, 58, 62))
              //Note: We just apply this warning to the bar as we don't have an arrow or anything here
              .warning(WarningType.INPUT_DOESNT_PRODUCE_OUTPUT, getWarningCheck(RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT));
        inputGauge = addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().inputTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 6, 13))
              .warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT));
        outputGauge = addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().outputTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 172, 13))
              .warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
    }

    private BooleanSupplier getWarningCheck(RecipeError error) {
        return () -> tile.getMultiblock().hasWarning(error);
    }

    @Override
    protected void addWarningTab(IWarningTracker warningTracker) {
        //Move the tab to the right side of the gui so it doesn't intersect the heat tab
        addRenderableWidget(new GuiWarningTab(this, warningTracker, false));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, inputGauge.getRelativeRight(), outputGauge.getRelativeX());
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}