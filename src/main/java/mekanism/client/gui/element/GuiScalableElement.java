package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.ResourceLocation;

public class GuiScalableElement extends GuiTexturedElement {

    public GuiScalableElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(resource, gui, x, y, width, height);
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        renderExtendedTexture(getResource(), 2, 2);
    }
}