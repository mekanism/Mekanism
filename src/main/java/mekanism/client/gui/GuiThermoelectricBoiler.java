package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiVerticalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGasGauge;
import mekanism.client.gui.element.tab.GuiBoilerTab;
import mekanism.client.gui.element.tab.GuiBoilerTab.BoilerTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiThermoelectricBoiler extends GuiMekanismTile<TileEntityBoilerCasing, MekanismTileContainer<TileEntityBoilerCasing>> {

    public GuiThermoelectricBoiler(MekanismTileContainer<TileEntityBoilerCasing> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        xSize += 40;
        playerInventoryTitleY += 2;
        titleY = 5;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 60, 23, 96, 40, () -> {
            BoilerMultiblockData multiblock = tile.getMultiblock();
            return Arrays.asList(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(multiblock.getTotalTemperature(), TemperatureUnit.KELVIN, true)),
                  MekanismLang.BOIL_RATE.translate(TextUtils.format(multiblock.lastBoilRate)), MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(multiblock.lastMaxBoil)));
        }));
        addButton(new GuiBoilerTab(this, tile, BoilerTab.STAT));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.BOIL_RATE.translate(TextUtils.format(tile.getMultiblock().lastBoilRate));
            }

            @Override
            public double getLevel() {
                BoilerMultiblockData multiblock = tile.getMultiblock();
                return Math.min(1, multiblock.lastBoilRate / (double) multiblock.lastMaxBoil);
            }
        }, 44, 13));
        addButton(new GuiVerticalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismLang.MAX_BOIL_RATE.translate(TextUtils.format(tile.getMultiblock().lastMaxBoil));
            }

            @Override
            public double getLevel() {
                BoilerMultiblockData multiblock = tile.getMultiblock();
                return Math.min(1, multiblock.lastMaxBoil * HeatUtils.getWaterThermalEnthalpy() /
                                   (multiblock.superheatingElements * MekanismConfig.general.superheatingHeatTransfer.get()));
            }
        }, 164, 13));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().superheatedCoolantTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 6, 13)
              .setLabel(MekanismLang.BOILER_HEATED_COOLANT_TANK.translateColored(EnumColor.ORANGE)));
        addButton(new GuiFluidGauge(() -> tile.getMultiblock().waterTank, () -> tile.getMultiblock().getFluidTanks(null), GaugeType.STANDARD, this, 26, 13)
              .setLabel(MekanismLang.BOILER_WATER_TANK.translateColored(EnumColor.INDIGO)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().steamTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 172, 13)
              .setLabel(MekanismLang.BOILER_STEAM_TANK.translateColored(EnumColor.GRAY)));
        addButton(new GuiGasGauge(() -> tile.getMultiblock().cooledCoolantTank, () -> tile.getMultiblock().getGasTanks(null), GaugeType.STANDARD, this, 192, 13)
              .setLabel(MekanismLang.BOILER_COOLANT_TANK.translateColored(EnumColor.AQUA)));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.getMultiblock().lastEnvironmentLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, MekanismLang.BOILER.translate(), titleY);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}