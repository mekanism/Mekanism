package mekanism.client.gui.element.progress;

import mekanism.client.gui.IGuiWrapper;

public class GuiFlame extends GuiProgress {

    public GuiFlame(IProgressInfoHandler handler, IGuiWrapper gui, int x, int y) {
        super(handler, ProgressType.FLAME, gui, x, y);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, type.getTextureWidth(), type.getTextureHeight());
        if (handler.isActive()) {
            int displayInt = (int) (handler.getProgress() * field_230689_k_);
            blit(field_230690_l_, field_230691_m_ + field_230689_k_ - displayInt, field_230688_j_, field_230689_k_ - displayInt, field_230688_j_, displayInt, type.getTextureWidth(), type.getTextureHeight());
        }
    }
}