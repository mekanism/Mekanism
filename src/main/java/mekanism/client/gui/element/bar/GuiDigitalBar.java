package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiDigitalBar extends GuiBar<IBarInfoHandler> {

    private static final Identifier DIGITAL_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_digital.png");
    private static final int texWidth = 2, texHeight = 2;

    public GuiDigitalBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        super(DIGITAL_BAR, gui, handler, x, y, width - 2, 6, true);
    }

    @Override
    protected void renderBarOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Render the bar
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DIGITAL_BAR, relativeX, relativeY, 1, 0, width, height, 1, 1, texWidth, texHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DIGITAL_BAR, relativeX + 1, relativeY + 1, 1, 1, width - 2, 6, 1, 1, texWidth, texHeight);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DIGITAL_BAR, relativeX + 1, relativeY + 1, 0, 0, calculateScaled(getHandler().getLevel(), width - 2), 6, 1, 1, texWidth, texHeight);
    }
}
