package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiBoilerTab;
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
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 40, 27, 96, 32));
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
        }, 24, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil());
            }

            @Override
            public double getLevel() {
                return tile.structure == null ? 0 : tile.getLastMaxBoil() * HeatUtils.getVaporizationEnthalpy() /
                    (tile.getSuperheatingElements() * MekanismConfig.general.superheatingHeatTransfer.get());
            }
        }, 144, 13));
        addButton(new GuiFluidGauge(() -> tile.structure == null ? null : tile.structure.waterTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiGasGauge(() -> tile.structure == null ? null : tile.structure.steamTank,
              () -> tile.structure == null ? Collections.emptyList() : tile.structure.getGasTanks(null), GaugeType.STANDARD, this, 152, 13));
        addButton(new GuiHeatInfo(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, 0x404040);
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 5, 0x404040);
        renderScaledText(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.KELVIN, true)), 43, 30, 0x00CD00, 90);
        renderScaledText(MekanismLang.BOIL_RATE.translate(tile.getLastBoilRate()), 43, 39, 0x00CD00, 90);
        renderScaledText(MekanismLang.MAX_BOIL_RATE.translate(tile.getLastMaxBoil()), 43, 48, 0x00CD00, 90);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}