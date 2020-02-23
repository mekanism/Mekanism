package mekanism.client.gui;

import java.util.Collections;
import mekanism.client.gui.element.progress.GuiFlame;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.progress.IProgressInfoHandler;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityFuelwoodHeater;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiFuelwoodHeater extends GuiMekanismTile<TileEntityFuelwoodHeater, MekanismTileContainer<TileEntityFuelwoodHeater>> {

    public GuiFuelwoodHeater(MekanismTileContainer<TileEntityFuelwoodHeater> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 48, 23, 80, 28));
        addButton(new GuiFlame(new IProgressInfoHandler() {
            @Override
            public double getProgress() {
                return tile.burnTime / (double) tile.maxBurnTime;
            }

            @Override
            public boolean isActive() {
                return tile.burnTime > 0;
            }
        }, this, 144, 31));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.lastEnvironmentLoss * unit.intervalSize, unit, false);
            return Collections.singletonList(MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        renderScaledText(MekanismLang.TEMPERATURE.translate(MekanismUtils.getTemperatureDisplay(tile.getTemp(), TemperatureUnit.AMBIENT)), 50, 25, 0x00CD00, 76);
        renderScaledText(MekanismLang.FUEL.translate(tile.burnTime), 50, 41, 0x00CD00, 76);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}