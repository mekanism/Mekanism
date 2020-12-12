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
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiCraftingWindowTab<DATA_SOURCE> extends GuiWindowCreatorTab<DATA_SOURCE, GuiCraftingWindowTab<DATA_SOURCE>> {

    private static final int MAX_WINDOWS = 3;
    private final Consumer<GuiCraftingWindow<DATA_SOURCE>> onFocus;
    //TODO: Evaluate a better way of doing this than this weird openWindows thing
    private final boolean[] openWindows = new boolean[MAX_WINDOWS];
    private int currentWindows;

    public GuiCraftingWindowTab(IGuiWrapper gui, DATA_SOURCE dataSource, Supplier<GuiCraftingWindowTab<DATA_SOURCE>> elementSupplier,
          Consumer<GuiCraftingWindow<DATA_SOURCE>> onFocus) {
        super(MekanismUtils.getResource(ResourceType.GUI_BUTTON, "crafting.png"), gui, dataSource, -26, 34, 26, 18, true, elementSupplier);
        this.onFocus = onFocus;
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
            if (tab.currentWindows < MAX_WINDOWS) {
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
        if (currentWindows >= MAX_WINDOWS) {
            //If we have the max number of windows we are allowed then disable the tab
            super.disableTab();
        }
    }

    @Override
    protected GuiWindow createWindow() {
        int index = 0;
        for (int i = 0; i < openWindows.length; i++) {
            if (!openWindows[i]) {
                index = i;
                break;
            }
        }
        openWindows[index] = true;
        //TODO: Fix indexing after exiting JEI it gets screwed up and resets
        // It does this because onClose is called, which means we then think the window is closed
        // To fix this would mean we have to keep track of how things close and maybe just not fire
        // the on close stuff if everything gets closed? Could lead to things that are supposed to
        // save on close not happening
        // TODO: Replace what calls onClose to call it with a "source"??? ^ may not even be true actually
        return new GuiCraftingWindow<>(guiObj, guiObj.getWidth() / 2 - 156 / 2, 15, dataSource, onFocus, index);
    }
}