package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class GuiScalableElement extends GuiTexturedElement {

    protected final int sideWidth;
    protected final int sideHeight;

    protected GuiScalableElement(Identifier resource, IGuiWrapper gui, int x, int y, int width, int height, int sideWidth, int sideHeight) {
        super(resource, gui, x, y, width, height);
        active = false;
        this.sideWidth = sideWidth;
        this.sideHeight = sideHeight;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(guiGraphics, getResource(), sideWidth, sideHeight);
    }
}