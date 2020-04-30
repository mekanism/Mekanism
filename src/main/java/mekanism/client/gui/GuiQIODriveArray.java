package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.content.qio.TileEntityQIODriveArray;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIODriveArray extends GuiMekanismTile<TileEntityQIODriveArray, MekanismTileContainer<TileEntityQIODriveArray>> {

    public GuiQIODriveArray(MekanismTileContainer<TileEntityQIODriveArray> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}