package mekanism.client.gui.element.tab.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiCraftingWindowTab<DATA_SOURCE> extends GuiWindowCreatorTab<DATA_SOURCE, GuiCraftingWindowTab<DATA_SOURCE>> {

    private static final int MAX_WINDOWS = 3;
    private int currentWindows;

    public GuiCraftingWindowTab(IGuiWrapper gui, DATA_SOURCE dataSource, Supplier<GuiCraftingWindowTab<DATA_SOURCE>> elementSupplier) {
        super(MekanismUtils.getResource(ResourceType.GUI_BUTTON, "crafting.png"), gui, dataSource, -26, 34, 26, 18, true, elementSupplier);
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
    protected Runnable getCloseListener() {
        return () -> {
            GuiCraftingWindowTab<DATA_SOURCE> tab = getElementSupplier().get();
            tab.currentWindows--;
            if (tab.currentWindows < MAX_WINDOWS) {
                //If we have less than the max number of windows re-enable the tab
                tab.active = true;
            }
        };
    }

    @Override
    protected void disableTab() {
        currentWindows++;
        if (currentWindows >= MAX_WINDOWS) {
            //If we have the max number of windows we are allowed then disable the tab
            super.disableTab();
        }
    }

    @Override
    protected GuiWindow createWindow() {
        return new GuiCraftingWindow<>(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, dataSource);
    }
}