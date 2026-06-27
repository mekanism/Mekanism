package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.lib.Color.ColorFunction;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiDynamicHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    //TODO - 26.2: Try to move this to the gui atlas
    private static final Identifier RATE_BAR = Mekanism.rl("gui/bar/dynamic_rate.png");
    private static final int texWidth = 3;
    private static final int texHeight = 8;

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
        int displayInt = calculateSize(handlerLevel, barWidth);
        for (int i = 0; i < displayInt; i++) {
            float level = i / (float) barWidth;
            int color = colorFunction.getColor(level).argb();
            if (i == 0) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1, relativeY + 1, 0, 0, 1, texHeight, texWidth, texHeight, color);
            } else if (i == displayInt - 1) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1 + i, relativeY + 1, texWidth - 1, 0, 1, texHeight, texWidth, texHeight, color);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1 + i, relativeY + 1, 1, 0, 1, texHeight, texWidth, texHeight, color);
            }
        }
    }
}