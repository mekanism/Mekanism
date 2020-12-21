package mekanism.client.gui.element.tab.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.tile.QIODashboardContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiCraftingWindowTab<DATA_SOURCE> extends GuiWindowCreatorTab<DATA_SOURCE, GuiCraftingWindowTab<DATA_SOURCE>> {

    //TODO: Evaluate a better way of doing this than this weird openWindows thing
    private final boolean[] openWindows = new boolean[QIOItemViewerContainer.MAX_CRAFTING_WINDOWS];
    private final QIODashboardContainer container;
    private byte currentWindows;

    public GuiCraftingWindowTab(IGuiWrapper gui, DATA_SOURCE dataSource, Supplier<GuiCraftingWindowTab<DATA_SOURCE>> elementSupplier, QIODashboardContainer container) {
        super(MekanismUtils.getResource(ResourceType.GUI_BUTTON, "crafting.png"), gui, dataSource, -26, 34, 26, 18, true, elementSupplier);
        this.container = container;
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        //TODO: Should this have its own translation key
        //TODO: Make it have a "subtext" part of the tooltip for displaying how many more windows can be created
        displayTooltip(matrix, MekanismLang.CRAFTING.translate(), mouseX, mouseY);
    }

    @Override
    protected void colorTab() {
        MekanismRenderer.color(SpecialColors.TAB_CRAFTING_WINDOW.get());
    }

    @Override
    protected Consumer<GuiWindow> getCloseListener() {
        return window -> {
            GuiCraftingWindowTab<DATA_SOURCE> tab = getElementSupplier().get();
            if (window instanceof GuiCraftingWindow) {
                tab.openWindows[((GuiCraftingWindow<?>) window).getIndex()] = false;
            }
            tab.currentWindows--;
            if (tab.currentWindows < QIOItemViewerContainer.MAX_CRAFTING_WINDOWS) {
                //If we have less than the max number of windows re-enable the tab
                tab.active = true;
            }
        };
    }

    @Override
    protected Consumer<GuiWindow> getReAttachListener() {
        return super.getReAttachListener().andThen(window -> {
            if (window instanceof GuiCraftingWindow) {
                GuiCraftingWindowTab<DATA_SOURCE> tab = getElementSupplier().get();
                tab.openWindows[((GuiCraftingWindow<?>) window).getIndex()] = true;
            }
        });
    }

    @Override
    protected void disableTab() {
        currentWindows++;
        if (currentWindows >= QIOItemViewerContainer.MAX_CRAFTING_WINDOWS) {
            //If we have the max number of windows we are allowed then disable the tab
            super.disableTab();
        }
    }

    @Override
    protected GuiWindow createWindow() {
        byte index = 0;
        for (int i = 0; i < openWindows.length; i++) {
            if (!openWindows[i]) {
                //Note: We cast it to a byte as it realistically will never be more than 2
                index = (byte) i;
                break;
            }
        }
        openWindows[index] = true;
        return new GuiCraftingWindow<>(gui(), getGuiWidth() / 2 - 156 / 2, 15, dataSource, container, index);
    }
}