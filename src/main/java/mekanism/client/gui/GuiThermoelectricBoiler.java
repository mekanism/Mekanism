package mekanism.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiThermoelectricBoiler extends GuiMekanismTile<TileEntityBoilerCasing, MekanismTileContainer<TileEntityBoilerCasing>> {

    public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityBoilerCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        xSize += 40;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 60, 23, 96, 40, () -> Arrays.asList(
            MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.KELVIN, true)),
            MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate()),
            MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil())
        )));
        addButton(new GuiBoilerTab(this, tile, BoilerTab.STAT));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate());
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : (double) tile.getLastBoilRate() / (double) tile.getLastMaxBoil();
            }
        }, 44, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil());
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastMaxBoil() * HeatUtils.getWaterThermalEnthalpy() /
                      (tile.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get());
            }
        }, 164, 13));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.superheatedCoolantTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 6, 13)
              .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.waterTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getFluidTanks(null), GaugeType.STANDARD, this, 26, 13)
              .setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.steamTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 172, 13)
              .setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.cooledCoolantTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 192, 13)
              .setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(MekanismLang.BOILER.translate(), 5);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}