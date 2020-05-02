package mekanism.client.gui.element.bar;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiDigitalBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation DIGITAL_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "dynamic_digital.png");
    private static final int texWidth = 2, texHeight = 2;

    public GuiDigitalBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int width) {
        super(DIGITAL_BAR, gui, handler, x, y, width - 2, 6);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {}

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Render the bar
        minecraft.textureManager.bindTexture(DIGITAL_BAR);
        blit(x, y, width, height, 1, 0, 1, 1, texWidth, texHeight);
        blit(x + 1, y + 1, width - 2, 6, 1, 1, 1, 1, texWidth, texHeight);
        blit(x + 1, y + 1, calculateScaled(getHandler().getLevel(), width - 2), 6, 0, 0, 1, 1, texWidth, texHeight);
    }
}
