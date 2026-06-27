package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiDigitalBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier DIGITAL_BAR = Mekanism.rl("bar/digital");
    private static final Identifier INTERNAL = Mekanism.rl("bar/digital_internal");

    public GuiDigitalBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        super(DIGITAL_BAR, gui, handler, x, y, width - 2, 6, true);
    }

    @Override
    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, INTERNAL, relativeX + 1, relativeY + 1, calculateSize(handlerLevel, width - 2), height - 2);
    }
}
