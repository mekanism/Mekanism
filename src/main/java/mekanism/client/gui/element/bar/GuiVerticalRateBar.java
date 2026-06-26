package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class GuiVerticalRateBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier RATE_BAR = Mekanism.rl("bar/vertical_rate");
    private static final int texWidth = 6;
    private static final int texHeight = 58;

    public GuiVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        super(gui, handler, x, y, texWidth, texHeight, false);
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        //Based on how AbstractFurnaceScreen calculates the flame progress height to always have at least 1 pixel showing if it is active
        int progressHeight = Mth.ceil(handlerLevel * (texHeight - 1)) + 1;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RATE_BAR, texWidth, texHeight, 0, texHeight - progressHeight,
              relativeX + 1, relativeY + 1 + texHeight - progressHeight, texWidth, progressHeight);
    }
}