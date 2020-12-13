package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.window.GuiCraftingWindowTab;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIODashboard extends GuiQIOItemViewer<QIODashboardContainer> {

    private final TileEntityQIODashboard tile;
    private GuiCraftingWindowTab<TileEntityQIODashboard> craftingWindowTab;

    public GuiQIODashboard(QIODashboardContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        tile = container.getTileEntity();
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
        //TODO: Figure out how we want to implement this into the portable QIO dashboard, as it may still need a decent
        // bit of refactoring to properly allow for updating the selected crafting grid window's index in the container
        addButton(craftingWindowTab = new GuiCraftingWindowTab<>(this, tile, () -> craftingWindowTab, window -> container.setSelectedCraftingGrid(window.getIndex())));
    }

    @Override
    public GuiQIOItemViewer<QIODashboardContainer> recreate(QIODashboardContainer container) {
        return new GuiQIODashboard(container, playerInventory, title);
    }

    @Override
    protected void transferWindows(Collection<GuiWindow> windows) {
        //Go through and adopt all the crafting windows so that we can
        // update the references that have for the listeners
        for (GuiWindow window : windows) {
            //TODO: Figure out a cleaner way of doing this that is more generic
            if (window instanceof GuiCraftingWindow) {
                craftingWindowTab.adoptWindows(window);
            }
        }
        // and then call super to transfer them over and call resize which will reattach it
        //TODO: Once we figure out how we are going to handle the datasource for the item viewer
        // we can just move this into super and it will be a lot cleaner
        super.transferWindows(windows);
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        drawTitleText(matrix, tile.getName(), titleY);
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public FrequencyIdentity getFrequency() {
        QIOFrequency freq = tile.getFrequency(FrequencyType.QIO);
        return freq != null ? freq.getIdentity() : null;
    }
}
