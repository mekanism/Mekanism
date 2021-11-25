package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.custom.GuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.IGuiColorFrequencySelector;
import mekanism.client.gui.element.custom.GuiFrequencySelector.ITileGuiFrequencySelector;
import mekanism.client.gui.element.custom.GuiTeleporterStatus;
import mekanism.common.content.teleporter.TeleporterFrequency;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiTeleporter extends GuiMekanismTile<TileEntityTeleporter, MekanismTileContainer<TileEntityTeleporter>>
      implements ITileGuiFrequencySelector<TeleporterFrequency, TileEntityTeleporter>, IGuiColorFrequencySelector<TeleporterFrequency> {

    public GuiTeleporter(MekanismTileContainer<TileEntityTeleporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        imageHeight += 74;
        titleLabelY = 4;
        inventoryLabelY = imageHeight - 93;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiTeleporterStatus(this, () -> getFrequency() != null, () -> tile.status));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 158, 26));
        addButton(new GuiFrequencySelector<>(this, 14));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyType<TeleporterFrequency> getFrequencyType() {
        return FrequencyType.TELEPORTER;
    }
}