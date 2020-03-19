package mekanism.generators.client.gui;

import java.util.Arrays;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiHeatInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.gauge.GaugeType;
import mekanism.client.gui.element.gauge.GuiFluidGauge;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.GeneratorsLang;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiHeatGenerator extends GuiMekanismTile<TileEntityHeatGenerator, MekanismTileContainer<TileEntityHeatGenerator>> {

    public GuiHeatGenerator(MekanismTileContainer<TileEntityHeatGenerator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiEnergyInfo(() -> Arrays.asList(
              GeneratorsLang.PRODUCING_AMOUNT.translate(EnergyDisplay.of(tile.getProducingEnergy())),
              MekanismLang.MAX_OUTPUT.translate(EnergyDisplay.of(tile.getMaxOutput()))), this));
        addButton(new GuiFluidGauge(() -> tile.lavaTank, GaugeType.WIDE, this, 55, 18));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 164, 15));
        addButton(new GuiHeatInfo(() -> {
            TemperatureUnit unit = EnumUtils.TEMPERATURE_UNITS[MekanismConfig.general.tempUnit.get().ordinal()];
            ITextComponent transfer = UnitDisplayUtils.getDisplayShort(tile.getLastTransferLoss(), unit, false);
            ITextComponent environment = UnitDisplayUtils.getDisplayShort(tile.getLastEnvironmentLoss(), unit, false);
            return Arrays.asList(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }, this));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), 45, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}