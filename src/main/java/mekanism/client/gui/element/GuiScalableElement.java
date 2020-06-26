package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.ResourceLocation;

public abstract class GuiScalableElement extends GuiTexturedElement {

    private final int sideWidth;
    private final int sideHeight;

    protected GuiScalableElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height, int sideWidth, int sideHeight) {
        super(resource, gui, x, y, width, height);
        field_230693_o_ = false;
        this.sideWidth = sideWidth;
        this.sideHeight = sideHeight;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        renderBackgroundTexture(getResource(), sideWidth, sideHeight);
    }
}