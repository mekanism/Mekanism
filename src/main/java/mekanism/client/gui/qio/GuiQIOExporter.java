package mekanism.client.gui.qio;

import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.custom.GuiQIOFrequencyDataScreen;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOExporter extends GuiMekanismTile<TileEntityQIOExporter, MekanismTileContainer<TileEntityQIOExporter>> {

    public GuiQIOExporter(MekanismTileContainer<TileEntityQIOExporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 40;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiQIOFrequencyDataScreen(this, 15, 19, xSize - 32, 46, () -> tile.getFrequency(FrequencyType.QIO)));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}