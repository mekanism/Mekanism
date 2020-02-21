package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiVerticalRateBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation RATE_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "vertical_rate.png");
    private static final int texWidth = 6;
    private static final int texHeight = 58;

    public GuiVerticalRateBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y) {
        //TODO: I believe the width and height will need to be changed/make it stretch
        super(RATE_BAR, gui, handler, x, y, texWidth, texHeight);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texHeight);
        //TODO: Should textureX be texWidth + 2
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y + height - 1 - displayInt, 8, height - 2 - displayInt, width - 2, displayInt, texWidth, texHeight);
    }
}