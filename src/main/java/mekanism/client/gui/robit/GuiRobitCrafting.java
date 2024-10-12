package mekanism.client.gui.robit;

import mekanism.client.gui.element.GuiRightArrow;
import mekanism.common.inventory.container.entity.robit.CraftingRobitContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiRobitCrafting extends GuiRobit<CraftingRobitContainer> {

    public GuiRobitCrafting(CraftingRobitContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        inventoryLabelY += 1;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiRightArrow(this, 90, 35).recipeViewerCrafting());
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.CRAFTING;
    }
}