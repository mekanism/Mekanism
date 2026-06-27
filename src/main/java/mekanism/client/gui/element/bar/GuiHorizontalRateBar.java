package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = Mekanism.rl("bar/horizontal_rate");

    public GuiHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(gui, handler, x, y, 78, 8, true);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int barWidth = width - 2;
        int barHeight = height - 2;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RATE_BAR, barWidth, barHeight, 0, 0, relativeX + 1, relativeY + 1,
              calculateSize(handlerLevel, barWidth), barHeight);
    }
}