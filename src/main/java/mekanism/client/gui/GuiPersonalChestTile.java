package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPersonalChestTile extends GuiMekanismTile<TileEntityPersonalChest, MekanismTileContainer<TileEntityPersonalChest>> {

    public GuiPersonalChestTile(MekanismTileContainer<TileEntityPersonalChest> container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 56;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}