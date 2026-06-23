package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiTextureOnlyElement extends GuiTexturedElement {

    public GuiTextureOnlyElement(Identifier resource, IGuiWrapper gui, int x, int y, int width, int height) {
        super(resource, gui, x, y, width, height);
        active = false;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, width, height);
    }
}