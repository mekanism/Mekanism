package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.lib.Color.ColorFunction;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiDynamicHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_rate.png");
    private static final int texWidth = 3;
    private static final int texHeight = 8;

    private final ColorFunction colorFunction;

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        this(gui, handler, x, y, width, ColorFunction.HEAT);
    }

    public GuiDynamicHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width, ColorFunction colorFunction) {
        super(gui, handler, x, y, width, texHeight, true);
        this.colorFunction = colorFunction;
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * (width - 2));
        if (displayInt > 0) {
            for (int i = 0; i < displayInt; i++) {
                float level = i / (float) (width - 2);
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
}