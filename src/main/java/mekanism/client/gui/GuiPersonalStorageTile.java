package mekanism.client.gui;

import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityPersonalStorage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiPersonalStorageTile extends GuiMekanismTile<TileEntityPersonalStorage, MekanismTileContainer<TileEntityPersonalStorage>> {

    public GuiPersonalStorageTile(MekanismTileContainer<TileEntityPersonalStorage> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 56;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}