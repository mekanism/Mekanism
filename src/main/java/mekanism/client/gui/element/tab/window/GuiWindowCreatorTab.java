package mekanism.client.gui.element.tab.window;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiWindowCreatorTab<DATA_SOURCE, ELEMENT extends GuiWindowCreatorTab<DATA_SOURCE, ELEMENT>> extends GuiInsetElement<DATA_SOURCE> {

    @NotNull
    private final Supplier<ELEMENT> elementSupplier;

    public GuiWindowCreatorTab(ResourceLocation overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left,
          @NotNull Supplier<ELEMENT> elementSupplier) {
        super(overlay, gui, dataSource, x, y, height, innerSize, left);
        this.elementSupplier = elementSupplier;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        openWindow(getNextWindowData());
    }

    private void openWindow(SelectedWindowData windowData) {
        GuiWindow window = createWindow(windowData);
        window.setTabListeners(getCloseListener(), getReAttachListener());
        disableTab();
        gui().addWindow(window);
    }

    @NotNull
    protected final Supplier<ELEMENT> getElementSupplier() {
        return elementSupplier;
    }

    public void adoptWindows(GuiWindow... windows) {
        for (GuiWindow window : windows) {
            window.setTabListeners(getCloseListener(), getReAttachListener());
        }
    }

    protected void disableTab() {
        active = false;
    }

    protected Consumer<GuiWindow> getCloseListener() {
        return window -> elementSupplier.get().active = true;
    }

    protected Consumer<GuiWindow> getReAttachListener() {
        return window -> elementSupplier.get().disableTab();
    }

    protected abstract GuiWindow createWindow(SelectedWindowData windowData);

    protected abstract SelectedWindowData getNextWindowData();

    protected List<SelectedWindowData> getValidWindows() {
        return List.of(getNextWindowData());
    }

    @Override
    public void openPinnedWindows() {
        super.openPinnedWindows();
        for (SelectedWindowData windowData : getValidWindows()) {
            if (windowData.wasPinned()) {
                openWindow(windowData);
            }
        }
    }
}
