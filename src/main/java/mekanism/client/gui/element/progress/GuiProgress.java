package mekanism.client.gui.element.progress;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;

public class GuiProgress extends GuiTexturedElement {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;

    public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.type = type;
        this.handler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (handler.isActive()) {
            minecraft.textureManager.bindTexture(getResource());
            guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
            if (type.isVertical()) {
                int displayInt = (int) (handler.getProgress() * height);
                guiObj.drawModalRectWithCustomSizedTexture(x, y, type.getOverlayX(), type.getOverlayY(), width, displayInt, type.getTextureWidth(), type.getTextureHeight());
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (handler.getProgress() * (width - 2 * innerOffsetX));
                guiObj.drawModalRectWithCustomSizedTexture(x + innerOffsetX, y, type.getOverlayX() + innerOffsetX, type.getOverlayY(), displayInt, height, type.getTextureWidth(), type.getTextureHeight());
            }
        }
    }
}