package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.gauge.GuiGauge;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.ThermalEvaporationControllerContainer;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiThermalEvaporationController extends GuiMekanismTile<TileEntityThermalEvaporationController, ThermalEvaporationControllerContainer> {

    public GuiThermalEvaporationController(ThermalEvaporationControllerContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        ResourceLocation resource = getGuiLocation();
        addButton(new GuiFluidGauge(() -> tile.inputTank, GuiGauge.Type.STANDARD, this, resource, 6, 13));
        addButton(new GuiFluidGauge(() -> tile.outputTank, GuiGauge.Type.STANDARD, this, resource, 152, 13));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.totalLoss * unit.intervalSize, unit, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this, resource));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.INVENTORY.translate(), 8, (ySize - 96) + 4, 0x404040);
        drawString(tile.getName(), (xSize / 2) - (getStringWidth(tile.getName()) / 2), 4, 0x404040);
        drawString(getStruct().translate(), 50, 21, 0x00CD00);
        drawString(MekanismLang.HEIGHT.translate(tile.height), 50, 30, 0x00CD00);
        drawString(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.AMBIENT)), 50, 39, 0x00CD00);
        renderScaledText(MekanismLang.FLUID_PRODUCTION.translate(Math.round(tile.lastGain * 100D) / 100D), 50, 48, 0x00CD00, 76);
        //TODO: 1.14 Convert to GuiElement
        int xAxis = mouseX - guiLeft;
        int yAxis = mouseY - guiTop;
        if (xAxis >= 49 && xAxis <= 127 && yAxis >= 64 && yAxis <= 72) {
            displayTooltip(MekanismUtils.getTemperatureDisplay(tile.getTemperature(), TemperatureUnit.AMBIENT), xAxis, yAxis);
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    private ILangEntry getStruct() {
        if (tile.structured) {
            return MekanismLang.MULTIBLOCK_FORMED;
        } else if (tile.controllerConflict) {
            return MekanismLang.MULTIBLOCK_CONFLICT;
        }
        return MekanismLang.MULTIBLOCK_INCOMPLETE;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        drawTexturedRect(guiLeft + 49, guiTop + 64, 176, 59, tile.getScaledTempLevel(78), 8);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "thermal_evaporation_controller.png");
    }
}