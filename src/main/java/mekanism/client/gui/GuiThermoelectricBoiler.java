package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiChemicalGauge;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiThermoelectricBoiler extends GuiMekanismTile<TileEntityBoilerCasing, MekanismTileContainer<TileEntityBoilerCasing>> {

    public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityBoilerCasing> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageWidth += 42;
        inventoryLabelX += 21;
        inventoryLabelY += 2;
        titleLabelY = 5;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiInnerScreen(this, 54, 23, 110, 40, () -> {
            BoilerMultiblockData multiblock = tile.getMultiblock();
            return List.of(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(multiblock.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
                  MekanismLang.BOIL_RATE.translate(TextUtils.format(multiblock.lastBoilRate)), MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(multiblock.lastMaxBoil)));
        }).padding(3).recipeViewerCategories(RecipeViewerRecipeType.BOILER));
        addRenderableWidget(new GuiBoilerTab(this, tile, BoilerTab.STAT));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.BOIL_RATE.translate(TextUtils.format(tile.getMultiblock().lastBoilRate));
            }

            @Override
            public double getLevel() {
                BoilerMultiblockData multiblock = tile.getMultiblock();
                return Math.min(1, multiblock.lastBoilRate / (double) multiblock.lastMaxBoil);
            }
        }, 44, 13));
        addRenderableWidget(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public Component getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(tile.getMultiblock().lastMaxBoil));
            }

            @Override
            public double getLevel() {
                BoilerMultiblockData multiblock = tile.getMultiblock();
                return Math.min(1, multiblock.lastMaxBoil * HeatUtils.getWaterThermalEnthalpy() /
                                   (multiblock.superheatingElements * MekanismConfig.general.superheatingHeatTransfer.get()));
            }
        }, 166, 13));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().superheatedCoolantTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 6, 13)
              .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addRenderableWidget(new GuiFluidGauge(() -> tile.getMultiblock().waterTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 26, 13)
              .setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().steamTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 174, 13)
              .setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        addRenderableWidget(new GuiChemicalGauge(() -> tile.getMultiblock().cooledCoolantTank, () -> tile.getMultiblock().getChemicalTanks((Direction) null), GaugeType.STANDARD, this, 194, 13)
              .setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void addWarningTab(IWarningTracker warningTracker) {
        //Move the tab to the right side of the gui so it doesn't intersect the heat tab
        addRenderableWidget(new GuiWarningTab(this, warningTracker, false));
    }
}