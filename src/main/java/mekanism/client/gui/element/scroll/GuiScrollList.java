package mekanism.client.gui.element.scroll;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiScalableElement;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public abstract class GuiScrollList extends GuiScrollableElement {

    protected static final ResourceLocation SCROLL_LIST = MekanismUtils.getResource(ResourceType.GUI, "scroll_list.png");
    protected static final int TEXTURE_WIDTH = 6;
    protected static final int TEXTURE_HEIGHT = 6;

    private final GuiScalableElement background;
    protected final int elementHeight;

    protected GuiScrollList(IGuiWrapper gui, int x, int y, int width, int height, int elementHeight, GuiScalableElement background) {
        super(SCROLL_LIST, gui, x, y, width, height, width - 6, 2, 4, 4, height - 4);
        this.elementHeight = elementHeight;
        this.background = background;
    }

    @Override
    protected int getFocusedElements() {
        return (height - 2) / elementHeight;
    }

    public abstract boolean hasSelection();

    protected abstract void setSelected(int index);

    public abstract void clearSelection();

    protected abstract void renderElements(MatrixStack matrix, int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw the background
        background.render(matrix, mouseX, mouseY, partialTicks);
        background.drawBackground(matrix, mouseX, mouseY, partialTicks);
        GuiElement.minecraft.textureManager.bindTexture(getResource());
        //Draw Scroll
        //Top border
        blit(matrix, barX - 1, barY - 1, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Middle border
        blit(matrix, barX - 1, barY, 6, maxBarHeight, 0, 1, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Bottom border
        blit(matrix, barX - 1, y + maxBarHeight + 2, 0, 0, TEXTURE_WIDTH, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Scroll bar
        blit(matrix, barX, barY + getScroll(), 0, 2, barWidth, barHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        //Draw the elements
        renderElements(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        if (mouseX >= x + 1 && mouseX < barX - 1 && mouseY >= y + 1 && mouseY < y + height - 1) {
            int index = getCurrentSelection();
            clearSelection();
            for (int i = 0; i < getFocusedElements(); i++) {
                if (index + i < getMaxElements()) {
                    int shiftedY = y + 1 + elementHeight * i;
                    if (mouseY >= shiftedY && mouseY <= shiftedY + elementHeight) {
                        setSelected(index + i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return isMouseOver(mouseX, mouseY) && adjustScroll(delta);
    }
}