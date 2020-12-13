package mekanism.client.gui.element.scroll;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiTexturedElement;
import net.minecraft.util.ResourceLocation;

public abstract class GuiScrollableElement extends GuiTexturedElement {

    protected double scroll;
    private boolean isDragging;
    private int dragOffset;
    protected final int barXShift;
    protected final int barX;
    protected final int barY;
    protected final int barWidth;
    protected final int barHeight;
    protected final int maxBarHeight;

    protected GuiScrollableElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height,
          int barXShift, int barYShift, int barWidth, int barHeight, int maxBarHeight) {
        super(resource, gui, x, y, width, height);
        this.barXShift = barXShift;
        this.barX = this.x + barXShift;
        this.barY = this.y + barYShift;
        this.barWidth = barWidth;
        this.barHeight = barHeight;
        this.maxBarHeight = maxBarHeight;
    }

    protected abstract int getMaxElements();

    protected abstract int getFocusedElements();

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        int scroll = getScroll();
        if (mouseX >= barX && mouseX <= barX + barWidth && mouseY >= barY + scroll && mouseY <= barY + scroll + barHeight) {
            if (needsScrollBars()) {
                double yAxis = mouseY - getGuiTop();
                dragOffset = (int) (yAxis - (scroll + barY));
                //Mark that we are dragging so that we can continue to "drag" even if our mouse goes off of being over the element
                isDragging = true;
            } else {
                this.scroll = 0;
            }
        }
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        super.onDrag(mouseX, mouseY, mouseXOld, mouseYOld);
        if (needsScrollBars() && isDragging) {
            double yAxis = mouseY - getGuiTop();
            this.scroll = Math.min(Math.max((yAxis - barY - dragOffset) / getMax(), 0), 1);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        dragOffset = 0;
        isDragging = false;
    }

    protected boolean needsScrollBars() {
        return getMaxElements() > getFocusedElements();
    }

    private int getMax() {
        return maxBarHeight - barHeight;
    }

    protected int getScroll() {
        //Calculate thumb position along scrollbar
        int max = getMax();
        return Math.max(Math.min((int) (scroll * max), max), 0);
    }

    public int getCurrentSelection() {
        if (needsScrollBars()) {
            int size = getMaxElements() - getFocusedElements();
            return (int) ((size + 0.5) * scroll);
        }
        return 0;
    }

    public boolean adjustScroll(double delta) {
        if (delta != 0 && needsScrollBars()) {
            int elements = getMaxElements() - getFocusedElements();
            if (elements > 0) {
                if (delta > 0) {
                    delta = 1;
                } else {
                    delta = -1;
                }
                scroll = (float) (scroll - delta / elements);
                if (scroll < 0.0F) {
                    scroll = 0.0F;
                } else if (scroll > 1.0F) {
                    scroll = 1.0F;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiScrollableElement old = (GuiScrollableElement) element;
        if (needsScrollBars() && old.needsScrollBars()) {
            //Only copy scrolling if we need scroll bars and used to also need scroll bars
            scroll = old.scroll;
        }
        //Note: We don't care about dragging as there is no way for the user while continuing to have MC focussed can change the window size
        // switching into full screen makes MC lose focus briefly anyways so dragging events don't continue to fire so that is not a case
        // that we need to worry about
    }
}