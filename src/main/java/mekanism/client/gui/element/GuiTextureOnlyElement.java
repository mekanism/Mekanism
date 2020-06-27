package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
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
        field_230693_o_ = false;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, textureWidth, textureHeight);
    }
}