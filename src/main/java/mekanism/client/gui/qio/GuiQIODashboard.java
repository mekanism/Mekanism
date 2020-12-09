package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.GuiWindow;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.tab.window.GuiCraftingWindowTab;
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
        addButton(craftingWindowTab = new GuiCraftingWindowTab<>(this, tile, () -> craftingWindowTab));
    }

    @Override
    public GuiQIOItemViewer<QIODashboardContainer> recreate(QIODashboardContainer container) {
        return new GuiQIODashboard(container, playerInventory, title);
    }

    @Override
    protected void transferWindows(int prevLeft, int prevTop, Collection<GuiWindow> windows) {
        for (GuiWindow window : windows) {
            //Transition all current popup windows over to the new screen.
            addWindow(window);
            //TODO: Figure out a cleaner way of doing this that is more generic
            if (window instanceof GuiCraftingWindow) {
                craftingWindowTab.adoptWindows(prevLeft, prevTop, window);
            }
        }
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
