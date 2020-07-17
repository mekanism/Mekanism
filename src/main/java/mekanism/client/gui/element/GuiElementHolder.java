package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiElementHolder extends GuiScalableElement {

    public static final ResourceLocation HOLDER = MekanismUtils.getResource(ResourceType.GUI, "element_holder.png");

    public GuiElementHolder(IGuiWrapper gui, int x, int y, int width, int height) {
        super(HOLDER, gui, x, y, width, height, 2, 2);
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        renderBackgroundTexture(matrix, getResource(), sideWidth, sideHeight);
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
    }
}