package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.render.MekanismRenderPipelines;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiDynamicHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = Mekanism.rl("bar/dynamic_rate");

    private final ColorFunction colorFunction;

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        this(gui, handler, x, y, width, ColorFunction.HEAT);
    }

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, Color from, Color to) {
        this(gui, handler, x, y, width, ColorFunction.scale(from, to));
    }

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, ColorFunction colorFunction) {
        super(gui, handler, x, y, width, 8, true);
        this.colorFunction = colorFunction;
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int barWidth = width - 2;
        int barHeight = height - 2;
        int targetWidth = calculateSize(handlerLevel, barWidth);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1, relativeY + 1, targetWidth, barHeight);
        for (int i = 0; i < targetWidth; i++) {
            int x0 = relativeX + 1 + i;
            int y0 = relativeY + 1;
            guiGraphics.fill(MekanismRenderPipelines.GUI_DST_COLOR, x0, y0, x0 + 1, y0 + barHeight, colorFunction.getColor(i / (float) barWidth).argb());
        }
    }
}