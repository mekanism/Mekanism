package mekanism.client.gui.item;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiPersonalChestItem extends GuiMekanism<PersonalChestItemContainer> {

    public GuiPersonalChestItem(PersonalChestItemContainer container, Inventory inv, Component title) {
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