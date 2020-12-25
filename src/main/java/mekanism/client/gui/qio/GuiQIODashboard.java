package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIODashboard extends GuiQIOItemViewer<QIODashboardContainer> {

    private final TileEntityQIODashboard tile;

    public GuiQIODashboard(QIODashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tile = container.getTileEntity();
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
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, tile.getName(), titleY);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        QIOFrequency freq = tile.getFrequency(FrequencyType.QIO);
        return freq == null ? null : freq.getIdentity();
    }
}
