package mekanism.client.gui.element.scroll;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiScrollList extends GuiScrollableElement {

    public static final ResourceLocation SCROLL_LIST = MekanismUtils.getResource(ResourceType.GUI, "scroll_list.png");
    public static final int TEXTURE_WIDTH = 6;
    public static final int TEXTURE_HEIGHT = 6;

    private final ResourceLocation background;
    private final int backgroundSideSize;
    protected final int elementHeight;

    protected GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, ResourceLocation background, int backgroundSideSize) {
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

    protected abstract void renderElements(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the background
        renderBackgroundTexture(guiGraphics, background, backgroundSideSize, backgroundSideSize);
        //Draw Scroll
        drawScrollBar(guiGraphics, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Draw the elements
        renderElements(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        if (mouseX >= getX() + 1 && mouseX < getX() + barXShift - 1 && mouseY >= getY() + 1 && mouseY < getBottom() - 1) {
            int index = getCurrentSelection();
            int focused = getFocusedElements();
            int maxElements = getMaxElements();
            for (int i = 0; i < focused && index + i < maxElements; i++) {
                int shiftedY = getY() + 1 + elementHeight * i;
                if (mouseY >= shiftedY && mouseY <= shiftedY + elementHeight) {
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