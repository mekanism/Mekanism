package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiInnerScreen extends GuiTexturedElement {

    private static final ResourceLocation SCREEN = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inner_screen.png");

    public GuiInnerScreen(IGuiWrapper gui, int x, int y, int width, int height) {
        super(SCREEN, gui, x, y, width, height);
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        renderExtendedTexture(SCREEN, 2, 2);
    }
}