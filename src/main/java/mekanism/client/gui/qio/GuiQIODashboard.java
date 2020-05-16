package mekanism.client.gui.qio;

import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIODashboard extends GuiQIOItemViewer<QIODashboardContainer> {

    private TileEntityQIODashboard tile;

    public GuiQIODashboard(QIODashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tile = container.getTileEntity();
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
    }

    @Override
    public GuiQIOItemViewer<QIODashboardContainer> recreate(QIODashboardContainer container) {
        return new GuiQIODashboard(container, playerInventory, title);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawTitleText(tile.getName(), 5);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        QIOFrequency freq = tile.getFrequency(FrequencyType.QIO);
        return freq != null ? freq.getIdentity() : null;
    }
}
