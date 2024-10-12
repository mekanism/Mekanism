package mekanism.client.gui.robit;

import mekanism.common.inventory.container.entity.robit.RobitContainer;
import net.minecraft.client.gui.GuiGraphics;
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
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.INVENTORY;
    }
}