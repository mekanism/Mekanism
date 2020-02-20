package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiDownArrow;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.bar.GuiHorizontalRateBar;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController, MekanismTileContainer<TileEntityThermalEvaporationController>> {

    public GuiThermalEvaporationController(MekanismTileContainer<TileEntityThermalEvaporationController> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 19, 80, 40));
        addButton(new GuiDownArrow(this, 32, 39));
        addButton(new GuiDownArrow(this, 136, 39));
        addButton(new GuiHorizontalRateBar(this, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.AMBIENT);
            }

            @Override
            public double getLevel() {
                return Math.min(1, tile.getTemperature() / MekanismConfig.general.evaporationMaxTemp.get());
            }
        }, 48, 63));
        addButton(new GuiFluidGauge(() -> tile.inputTank, GuiGauge.Type.STANDARD, this, 6, 13));
        addButton(new GuiFluidGauge(() -> tile.outputTank, GuiGauge.Type.STANDARD, this, 152, 13));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.totalLoss * unit.intervalSize, unit, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 4, 0x404040);
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(getStruct().translate(), 50, 21, 0x00CD00);
        drawString(MekanismLang.HEIGHT.translate(tile.height), 50, 30, 0x00CD00);
        drawString(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.AMBIENT)), 50, 39, 0x00CD00);
        renderScaledText(MekanismLang.FLUID_PRODUCTION.translate(Math.round(tile.lastGain * 100D) / 100D), 50, 48, 0x00CD00, 76);
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

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "thermal_evaporation_controller.png");
    }
}