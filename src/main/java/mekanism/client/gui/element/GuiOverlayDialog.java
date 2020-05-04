package mekanism.client.gui.element;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.Color;
import net.minecraft.inventory.container.Container;

public class GuiOverlayDialog extends GuiTexturedElement {

    private static final Color OVERLAY_COLOR = Color.rgba(60, 60, 60, 128);

    public GuiOverlayDialog(IGuiWrapper gui, int x, int y, int width, int height) {
       super(GuiMekanism.BASE_BACKGROUND, gui, x, y, width, height);
       active = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        // always return true to prevent background clicking
        if (guiObj instanceof GuiMekanism && ((GuiMekanism<?>) guiObj).getContainer() != null) {
            Container c = ((GuiMekanism<?>) guiObj).getContainer();
            if (!(c instanceof IEmptyContainer)) {
                if (mouseX >= guiObj.getLeft() && mouseX < guiObj.getLeft() + guiObj.getWidth() && mouseY >= guiObj.getTop() + guiObj.getHeight() - 90) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {}

    @Override
    public void renderBackgroundOverlay(int mouseX, int mouseY) {
        if (renderOverlay()) {
            MekanismRenderer.renderColorOverlay(0, 0, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight(), OVERLAY_COLOR.rgba());
        }
        minecraft.textureManager.bindTexture(getResource());
        renderBackgroundTexture(getResource(), 4, 4);
    }

    protected void close() {
        guiObj.removeElement(this);
    }

    protected boolean renderOverlay() {
        return true;
    }
}
