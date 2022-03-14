package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.item.PersonalStorageItemContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPersonalStorageItem extends GuiMekanism<PersonalStorageItemContainer> {

    public GuiPersonalStorageItem(PersonalStorageItemContainer container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 56;
        inventoryLabelY = imageHeight - 94;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSecurityTab(this, menu.getHand()));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventoryTitle, inventoryLabelX, inventoryLabelY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}