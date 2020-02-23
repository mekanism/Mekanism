package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPersonalChestItem extends GuiMekanism<PersonalChestItemContainer> {

    public GuiPersonalChestItem(PersonalChestItemContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, container.getItemInventory().currentHand));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismBlocks.PERSONAL_CHEST.getTextComponent(), 8, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}