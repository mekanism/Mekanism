package mekanism.client.gui.robit;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRobitInventory extends GuiRobit<RobitContainer> {

    public GuiRobitInventory(RobitContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY = imageHeight - 93;
        dynamicSlots = true;
    }

    @Override
    protected void drawForegroundText(@NotNull PoseStack matrix, int mouseX, int mouseY) {
        drawString(matrix, title, titleLabelX, titleLabelY, titleTextColor());
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.INVENTORY;
    }
}