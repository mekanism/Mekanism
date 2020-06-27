package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
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
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        renderBackgroundTexture(matrix, getResource(), sideWidth, sideHeight);
    }
}