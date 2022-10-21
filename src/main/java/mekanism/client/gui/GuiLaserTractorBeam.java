package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiLaserTractorBeam extends GuiMekanismTile<TileEntityLaserTractorBeam, MekanismTileContainer<TileEntityLaserTractorBeam>> {

    public GuiLaserTractorBeam(MekanismTileContainer<TileEntityLaserTractorBeam> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}