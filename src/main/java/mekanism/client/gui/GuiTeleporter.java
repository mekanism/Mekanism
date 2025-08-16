package mekanism.client.gui;

import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IGuiColorFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.ITileGuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityTeleporter.TeleporterStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiTeleporter extends GuiMekanismTile<TileEntityTeleporter, MekanismTileContainer<TileEntityTeleporter>>
      implements ITileGuiFrequencySelector<TeleporterFrequency, TileEntityTeleporter>, IGuiColorFrequencySelector<TeleporterFrequency> {

    private GuiTeleporterStatus status;

    public GuiTeleporter(MekanismTileContainer<TileEntityTeleporter> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 74;
        titleLabelY = 4;
        inventoryLabelY = imageHeight - 93;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        status = addRenderableWidget(new GuiTeleporterStatus(this, () -> getFrequency() != null, () -> tile.status));
        addRenderableWidget(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 158, 26))
              .warning(WarningType.NOT_ENOUGH_ENERGY, () -> tile.status == TeleporterStatus.NOT_ENOUGH_ENERGY);
        addRenderableWidget(new GuiFrequencySelector<>(this, 14));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleTextWithOffset(guiGraphics, status.getRelativeRight(), tile.getEnergySlotX());
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    public FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }
}