package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class MekanismImageButton extends MekanismButton {

    private final Identifier resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, Identifier resource, @NotNull IClickable onPress) {
        this(gui, x, y, size, size, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, int textureSize, Identifier resource, @NotNull IClickable onPress) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, Identifier resource,
          @NotNull IClickable onPress) {
        this(gui, x, y, width, height, textureWidth, textureHeight, resource, onPress, onPress);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, Identifier resource,
          @NotNull IClickable onLeftClick, @NotNull IClickable onRightClick) {
        super(gui, x, y, width, height, CommonComponents.EMPTY, onLeftClick, onRightClick);
        this.resourceLocation = resource;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    protected Identifier getResource() {
        return resourceLocation;
    }
}