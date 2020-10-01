package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;

public class GuiFlame extends GuiProgress {

    public GuiFlame(IProgressInfoHandler handler, IGuiWrapper gui, int x, int y) {
        super(handler, ProgressType.FLAME, gui, x, y);
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(matrix, x, y, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
        if (handler.isActive()) {
            int displayInt = (int) (getProgress() * height);
            blit(matrix, x, y + height - displayInt, width, height - displayInt, width, displayInt, type.getTextureWidth(), type.getTextureHeight());
        }
    }
}