package mekanism.client.gui;

import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiLaserTractorBeam extends GuiMekanismTile<TileEntityLaserTractorBeam, MekanismTileContainer<TileEntityLaserTractorBeam>> {

    public GuiLaserTractorBeam(MekanismTileContainer<TileEntityLaserTractorBeam> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSecurityTab<>(this, tile));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}