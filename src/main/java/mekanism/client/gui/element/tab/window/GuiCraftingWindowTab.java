package mekanism.client.gui.element.tab.window;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiCraftingWindow;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;

public class GuiCraftingWindowTab extends GuiWindowCreatorTab<Void, GuiCraftingWindowTab> {

    private static final List<SelectedWindowData> VALID_WINDOWS = Util.make(() -> {
        List<SelectedWindowData> valid = new ArrayList<>(IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS);
        for (byte i = 0; i < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS; i++) {
            valid.add(new SelectedWindowData(WindowType.CRAFTING, i));
        }
        return List.copyOf(valid);
    });
    private static final Byte2ObjectMap<Tooltip> tooltips = new Byte2ObjectArrayMap<>(IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS);

    private final boolean[] openWindows = new boolean[IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS];
    private final QIOItemViewerContainer container;
    private byte currentWindows;

    public GuiCraftingWindowTab(IGuiWrapper gui, Supplier<GuiCraftingWindowTab> elementSupplier, QIOItemViewerContainer container) {
        super(MekanismUtils.getResource(ResourceType.GUI_BUTTON, "crafting.png"), gui, null, -26, 34, 26, 18, true, elementSupplier);
        this.container = container;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(tooltips.computeIfAbsent(currentWindows, c -> TooltipUtils.create(MekanismLang.CRAFTING_TAB.translate(c, IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS))));
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_CRAFTING_WINDOW.get());
    }

    @Override
    protected Consumer<GuiWindow> getCloseListener() {
        return window -> {
            GuiCraftingWindowTab tab = getElementSupplier().get();
            if (window instanceof GuiCraftingWindow craftingWindow) {
                tab.openWindows[craftingWindow.getIndex()] = false;
            }
            tab.currentWindows--;
            if (tab.currentWindows < IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
                //If we have less than the max number of windows re-enable the tab
                tab.active = true;
            }
        };
    }

    @Override
    protected Consumer<GuiWindow> getReAttachListener() {
        return super.getReAttachListener().andThen(window -> {
            if (window instanceof GuiCraftingWindow craftingWindow) {
                GuiCraftingWindowTab tab = getElementSupplier().get();
                tab.openWindows[craftingWindow.getIndex()] = true;
            }
        });
    }

    @Override
    protected void disableTab() {
        currentWindows++;
        if (currentWindows >= IQIOCraftingWindowHolder.MAX_CRAFTING_WINDOWS) {
            //If we have the max number of windows we are allowed then disable the tab
            super.disableTab();
        }
    }

    @Override
    protected SelectedWindowData getNextWindowData() {
        byte index = 0;
        for (int i = 0; i < openWindows.length; i++) {
            if (!openWindows[i]) {
                //Note: We cast it to a byte as it realistically will never be more than 2
                index = (byte) i;
                break;
            }
        }
        return new SelectedWindowData(WindowType.CRAFTING, index);
    }

    @Override
    protected List<SelectedWindowData> getValidWindows() {
        return VALID_WINDOWS;
    }

    @Override
    protected GuiWindow createWindow(SelectedWindowData windowData) {
        openWindows[windowData.extraData] = true;
        return new GuiCraftingWindow(gui(), (getGuiWidth() - 124) / 2, 15, container, windowData);
    }
}