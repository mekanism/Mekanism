package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiHorizontalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_rate.png");
    private static final int texWidth = 78;
    private static final int texHeight = 8;

    public GuiHorizontalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(gui, handler, x, y, texWidth, texHeight, true);
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * texWidth);
        if (displayInt > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1, relativeY + 1, 0, 0, displayInt, texHeight, texWidth, texHeight);
        }
    }
}