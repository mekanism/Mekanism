package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.ITileGuiFrequencySelector;
import mekanism.client.gui.element.tab.GuiEnergyTab;
import mekanism.client.gui.element.tab.GuiHeatTab;
import mekanism.client.gui.element.tab.GuiWarningTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.IWarningTracker;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQuantumEntangloporter extends GuiConfigurableTile<TileEntityQuantumEntangloporter, MekanismTileContainer<TileEntityQuantumEntangloporter>>
      implements ITileGuiFrequencySelector<InventoryFrequency, TileEntityQuantumEntangloporter> {

    public GuiQuantumEntangloporter(MekanismTileContainer<TileEntityQuantumEntangloporter> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 74;
        titleLabelY = 4;
        inventoryLabelY = imageHeight - 93;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
        addRenderableWidget(new GuiEnergyTab(this, () -> {
            InventoryFrequency frequency = getFrequency();
            EnergyDisplay storing = frequency == null ? EnergyDisplay.ZERO : EnergyDisplay.of(frequency.storedEnergy);
            EnergyDisplay rate = EnergyDisplay.of(tile.getInputRate());
            return List.of(MekanismLang.STORING.translate(storing), MekanismLang.MATRIX_INPUT_RATE.translate(rate));
        }));
        addRenderableWidget(new GuiHeatTab(this, () -> {
            Component transfer = MekanismUtils.getTemperatureDisplay(tile.getLastTransferLoss(), TemperatureUnit.KELVIN, false);
            Component environment = MekanismUtils.getTemperatureDisplay(tile.getLastEnvironmentLoss(), TemperatureUnit.KELVIN, false);
            return List.of(MekanismLang.TRANSFERRED_RATE.translate(transfer), MekanismLang.DISSIPATED_RATE.translate(environment));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public FrequencyType<InventoryFrequency> getFrequencyType() {
        return FrequencyType.INVENTORY;
    }

    @Override
    protected void addWarningTab(IWarningTracker warningTracker) {
        //Move the tab to the right side of the gui so it doesn't intersect the heat tab
        addRenderableWidget(new GuiWarningTab(this, warningTracker, false));
    }
}