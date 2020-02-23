package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.ResourceLocation;

public class GuiTextureOnlyElement extends GuiTexturedElement {

    private final int textureWidth;
    private final int textureHeight;

    public GuiTextureOnlyElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height) {
        this(resource, gui, x, y, width, height, width, height);
    }

    public GuiTextureOnlyElement(ResourceLocation resource, IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight) {
        super(resource, gui, x, y, width, height);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        active = false;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, textureWidth, textureHeight);
    }
}