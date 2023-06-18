package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import net.minecraft.client.gui.GuiGraphics;

public class GuiEmptyBar extends GuiBar<IBarInfoHandler> {

    private static final IBarInfoHandler EMPTY_INFO = () -> 0;

    public GuiEmptyBar(IGuiWrapper gui, int x, int y, int width, int height) {
        super(null, gui, EMPTY_INFO, x, y, width, height, width >= height);
        //Pass null as we technically don't need a resource given our handler always gives a zero size
    }

    @Override
    protected void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
    }
}