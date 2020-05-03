package mekanism.client.gui;

import java.util.Arrays;
import java.util.Collections;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController, MekanismTileContainer<TileEntityThermalEvaporationController>> {

    public GuiThermalEvaporationController(MekanismTileContainer<TileEntityThermalEvaporationController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 19, 80, 40, () -> Arrays.asList(
            getStruct().translate(),
            MekanismLang.HEIGHT.translate(tile.height),
            MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemp(), TemperatureUnit.KELVIN, true)),
            MekanismLang.FLUID_PRODUCTION.translate(Math.round(tile.lastGain * 100D) / 100D)
        )).spacing(1));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiDownArrow(this, 136, 39));
        addButton(new GuiHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getTemp(), TemperatureUnit.KELVIN, true);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getTemp() / TileEntityThermalEvaporationController.MAX_MULTIPLIER_TEMP);
            }
        }, 48, 63));
        addButton(new GuiFluidGauge(() -> tile.inputTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 6, 13));
        addButton(new GuiFluidGauge(() -> tile.outputTank, () -> tile.getFluidTanks(null), GaugeType.STANDARD, this, 152, 13));
        addButton(new GuiHeatTab(() -> {
            ITextComponent environment = MekanismUtils.getTemperatureDisplay(tile.totalLoss, TemperatureUnit.KELVIN, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText(4);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private ILangEntry getStruct() {
        if (tile.getActive()) {
            return MekanismLang.MULTIBLOCK_FORMED;
        } else if (tile.controllerConflict) {
            return MekanismLang.MULTIBLOCK_CONFLICT;
        }
        return MekanismLang.MULTIBLOCK_INCOMPLETE;
    }
}