package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiVerticalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "vertical_rate.png");
    private static final int texWidth = 6;
    private static final int texHeight = 58;

    public GuiVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(gui, handler, x, y, texWidth, texHeight, false);
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        int displayInt = (int) (handlerLevel * texHeight);
        if (displayInt > 0) {
            //TODO: Should textureX be texWidth + 2
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RATE_BAR, relativeX + 1, relativeY + height - 1 - displayInt, 8, height - 2 - displayInt, width - 2, displayInt, texWidth, texHeight);
        }
    }
}