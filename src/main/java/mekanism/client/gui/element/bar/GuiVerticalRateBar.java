package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiVerticalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = Mekanism.rl("bar/vertical_rate");

    public GuiVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(gui, handler, x, y, 6, 58, false);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int barHeight = height - 2;
        int barWidth = width - 2;
        int progressHeight = calculateSize(handlerLevel, barHeight);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RATE_BAR, barWidth, barHeight, 0, barHeight - progressHeight,
              relativeX + 1, relativeY + 1 + barHeight - progressHeight, barWidth, progressHeight);
    }
}