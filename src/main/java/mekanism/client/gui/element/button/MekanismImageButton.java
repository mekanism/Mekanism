package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

public class MekanismImageButton extends MekanismButton {

    private final Identifier resourceLocation;

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, Identifier resource, IClickable onPress) {
        this(gui, x, y, size, size, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, Identifier resource, IClickable onPress) {
        this(gui, x, y, width, height, resource, onPress, onPress);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, Identifier resource, IClickable onLeftClick, IClickable onRightClick) {
        super(gui, x, y, width, height, CommonComponents.EMPTY, onLeftClick, onRightClick);
        this.resourceLocation = resource;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    protected Identifier getResource() {
        return resourceLocation;
    }
}