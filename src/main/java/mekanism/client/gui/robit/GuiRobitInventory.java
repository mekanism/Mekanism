package mekanism.client.gui.robit;

import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.entity.robit.InventoryRobitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiRobitInventory extends GuiRobit<InventoryRobitContainer> {

    public GuiRobitInventory(InventoryRobitContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(MekanismLang.ROBIT_INVENTORY.translate(), 8, 6, titleTextColor());
        drawString(MekanismLang.INVENTORY.translate(), 8, getYSize() - 93, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected boolean shouldOpenGui(RobitGuiType guiType) {
        return guiType != RobitGuiType.INVENTORY;
    }
}