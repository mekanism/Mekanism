package mekanism.client.gui.element.progress;

import mekanism.client.gui.IGuiWrapper;

public class GuiFlame extends GuiProgress {

    public GuiFlame(IProgressInfoHandler handler, IGuiWrapper gui, int x, int y) {
        super(handler, ProgressType.FLAME, gui, x, y);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
        if (handler.isActive()) {
            int displayInt = (int) (handler.getProgress() * height);
            guiObj.drawModalRectWithCustomSizedTexture(x, y + height - displayInt, width, height - displayInt, width, displayInt, type.getTextureWidth(), type.getTextureHeight());
        }
    }
}