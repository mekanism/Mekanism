package mekanism.client.gui.element.scroll;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class GuiScrollList extends GuiScrollableElement {

    public static final Identifier SCROLL_LIST = MekanismUtils.getResource(ResourceType.GUI, "scroll_list.png");
    public static final int TEXTURE_WIDTH = 6;
    public static final int TEXTURE_HEIGHT = 6;

    private final Identifier background;
    private final int backgroundSideSize;
    protected final int elementHeight;

    protected GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, Identifier background, int backgroundSideSize) {
        super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.elementHeight = elementHeight;
        this.background = background;
        this.backgroundSideSize = backgroundSideSize;
    }

    @Override
    protected int getFocusedElements() {
        return (height - 2) / elementHeight;
    }

    public abstract boolean hasSelection();

    protected abstract void setSelected(int index);

    public abstract void clearSelection();

    protected abstract void renderElements(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the background
        renderBackgroundTexture(guiGraphics, background, backgroundSideSize, backgroundSideSize);
        //Draw Scroll
        drawScrollBar(guiGraphics, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Draw the elements
        renderElements(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        super.onClick(event, isDoubleClick);
        if (event.x() >= getX() + 1 && event.x() < getX() + barXShift - 1 && event.y() >= getY() + 1 && event.y() < getBottom() - 1) {
            int index = getCurrentSelection();
            int focused = getFocusedElements();
            int maxElements = getMaxElements();
            for (int i = 0; i < focused && index + i < maxElements; i++) {
                int shiftedY = getY() + 1 + elementHeight * i;
                if (event.y() >= shiftedY && event.y() <= shiftedY + elementHeight) {
                    setSelected(index + i);
                    return;
                }
            }
            //Only clear the selection if we clicked in the area but not on a selectable index
            clearSelection();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return isMouseOver(mouseX, mouseY) && adjustScroll(yDelta) || super.mouseScrolled(mouseX, mouseY, xDelta, yDelta);
    }
}