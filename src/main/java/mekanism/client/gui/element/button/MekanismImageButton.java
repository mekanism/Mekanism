package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class MekanismImageButton extends MekanismButton {

    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, Runnable onPress) {
        this(gui, x, y, size, size, resource, onPress);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, Runnable onPress, IHoverable onHover) {
        this(gui, x, y, size, size, resource, onPress, onHover);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, Runnable onPress) {
        this(gui, x, y, size, textureSize, resource, onPress, null);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, Runnable onPress, IHoverable onHover) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, onPress, onHover);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource, Runnable onPress) {
        this(gui, x, y, width, height, textureWidth, textureHeight, resource, onPress, null);
    }

    public MekanismImageButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource, Runnable onPress, IHoverable onHover) {
        super(gui, x, y, width, height, StringTextComponent.EMPTY, onPress, onHover);
        this.resourceLocation = resource;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        MekanismRenderer.bindTexture(getResource());
        blit(matrix, x, y, width, height, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    protected ResourceLocation getResource() {
        return resourceLocation;
    }
}