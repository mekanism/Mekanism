package mekanism.client.gui.element;

import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;

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
        return true;
    }

    @Override
    public void drawButton(int mouseX, int mouseY) {}

    @Override
    public void renderBackgroundOverlay(int mouseX, int mouseY) {
        MekanismRenderer.renderColorOverlay(0, 0, minecraft.getMainWindow().getScaledWidth(), minecraft.getMainWindow().getScaledHeight(), OVERLAY_COLOR.rgba());
    }
}
