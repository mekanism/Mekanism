package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MekanismImageButton extends MekanismButton {

    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, @NotNull IClickable onPress) {
        this(gui, x, y, size, size, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, @NotNull IClickable onPress) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource,
          @NotNull IClickable onPress) {
        this(gui, x, y, width, height, textureWidth, textureHeight, resource, onPress, onPress);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource,
          @NotNull IClickable onLeftClick, @NotNull IClickable onRightClick) {
        super(gui, x, y, width, height, CommonComponents.EMPTY, onLeftClick, onRightClick);
        this.resourceLocation = resource;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(getResource(), getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight(), 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    protected ResourceLocation getResource() {
        return resourceLocation;
    }
}