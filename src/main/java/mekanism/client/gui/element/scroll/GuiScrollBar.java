package mekanism.client.gui.element.scroll;

import java.util.function.IntSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiScrollBar extends GuiScrollableElement {

    private static final Identifier BAR = Mekanism.rl("scroll/bar");
    private static final Identifier BAR_INACTIVE = Mekanism.rl("scroll/bar_inactive");

    private final IntSupplier maxElements;
    private final IntSupplier focusedElements;

    public GuiScrollBar(IGuiWrapper gui, int x, int y, int height, IntSupplier maxElements, IntSupplier focusedElements) {
        super(BAR, gui, x, y, 14, height, 1, 1, 12, 15, height - 2);
        this.maxElements = maxElements;
        this.focusedElements = focusedElements;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw background and border
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiElementHolder.HOLDER, relativeX, relativeY, barWidth + 2, height);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, needsScrollBars() ? BAR : BAR_INACTIVE, barX, barY + getScroll(), barWidth, barHeight);
    }

    @Override
    protected int getMaxElements() {
        return maxElements.getAsInt();
    }

    @Override
    protected int getFocusedElements() {
        return focusedElements.getAsInt();
    }
}