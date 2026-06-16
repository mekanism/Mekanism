package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public abstract class GuiScalableElement extends GuiTexturedElement {

    //TODO - 26.2: move side width/height into sprite mcmeta
    protected GuiScalableElement(Identifier resource, IGuiWrapper gui, int x, int y, int width, int height, int sideWidth, int sideHeight) {
        super(resource, gui, x, y, width, height);
        active = false;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }
}