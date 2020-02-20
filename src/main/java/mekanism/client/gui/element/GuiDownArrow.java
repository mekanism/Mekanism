package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiDownArrow extends GuiTexturedElement {

    private static final ResourceLocation ARROW = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "down_arrow.png");

    public GuiDownArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 8, 9);
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
    }
}