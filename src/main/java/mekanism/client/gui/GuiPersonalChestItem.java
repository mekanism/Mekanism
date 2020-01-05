package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.item.PersonalChestItemContainer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiPersonalChestItem extends GuiMekanism<PersonalChestItemContainer> {

    public GuiPersonalChestItem(PersonalChestItemContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, getGuiLocation(), container.getItemInventory().currentHand));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismBlocks.PERSONAL_CHEST.getTextComponent(), 8, 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "personal_chest.png");
    }
}