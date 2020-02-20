package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiProgress extends GuiTexturedElement {

    private final IProgressInfoHandler handler;
    private final ProgressBar type;

    public GuiProgress(IProgressInfoHandler handler, ProgressBar type, IGuiWrapper gui, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "progress.png"), gui, x, y, type.width, type.height);
        this.type = type;
        this.handler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        if (handler.isActive()) {
            minecraft.textureManager.bindTexture(getResource());
            guiObj.drawTexturedRect(x, y, type.textureX, type.textureY, width, height);
            int innerOffsetX = 2;
            int displayInt = (int) (handler.getProgress() * (width - 2 * innerOffsetX));
            guiObj.drawTexturedRect(x + innerOffsetX, y, type.textureX + width + innerOffsetX, type.textureY, displayInt, height);
        }
    }

    public enum ProgressBar {
        BAR(28, 11, 0, 0),

        LARGE_RIGHT(52, 10, 128, 0),
        LARGE_LEFT(52, 10, 128, 10),
        MEDIUM(36, 10, 128, 20),
        SMALL_RIGHT(32, 10, 128, 30),
        SMALL_LEFT(32, 10, 128, 40),
        BI(20, 8, 128, 50);

        private final int width;
        private final int height;

        private final int textureX;
        private final int textureY;

        ProgressBar(int width, int height, int textureX, int textureY) {
            this.width = width;
            this.height = height;
            this.textureX = textureX;
            this.textureY = textureY;
        }
    }

    public interface IProgressInfoHandler {

        double getProgress();

        default boolean isActive() {
            return true;
        }
    }
}