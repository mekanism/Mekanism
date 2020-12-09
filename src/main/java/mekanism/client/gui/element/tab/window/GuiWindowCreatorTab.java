package mekanism.client.gui.element.tab.window;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiWindow;
import net.minecraft.util.ResourceLocation;

public abstract class GuiWindowCreatorTab<DATA_SOURCE, ELEMENT extends GuiWindowCreatorTab<DATA_SOURCE, ELEMENT>> extends GuiInsetElement<DATA_SOURCE> {

    @Nonnull
    private final Supplier<ELEMENT> elementSupplier;

    public GuiWindowCreatorTab(ResourceLocation overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left,
          @Nonnull Supplier<ELEMENT> elementSupplier) {
        super(overlay, gui, dataSource, x, y, height, innerSize, left);
        this.elementSupplier = elementSupplier;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        GuiWindow window = createWindow();
        window.setTabListeners(getCloseListener(), getReAttachListener());
        disableTab();
        guiObj.addWindow(window);
    }

    @Nonnull
    protected final Supplier<ELEMENT> getElementSupplier() {
        return elementSupplier;
    }

    public void adoptWindows(int prevLeft, int prevTop, GuiWindow...windows) {
        int left = guiObj.getLeft();
        int top = guiObj.getTop();
        for (GuiWindow window : windows) {
            Runnable reattachListener = getReAttachListener();
            //TODO: Fix the windows after being adopted not being able to be closed??
            window.setTabListeners(getCloseListener(), reattachListener);
            //TODO: Fix positioning
            //reattachListener.run();
            window.resize(prevLeft, prevTop, left, top);
        }
    }

    protected void disableTab() {
        active = false;
    }

    protected Runnable getCloseListener() {
        return () -> elementSupplier.get().active = true;
    }

    protected Runnable getReAttachListener() {
        return () -> elementSupplier.get().disableTab();
    }

    protected abstract GuiWindow createWindow();
}
