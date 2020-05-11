package mekanism.client.gui.element;

import org.lwjgl.glfw.GLFW;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.container.IEmptyContainer;
import mekanism.common.lib.Color;
import net.minecraft.inventory.container.Container;

public class GuiOverlayDialog extends GuiTexturedElement {

    private static final Color OVERLAY_COLOR = Color.rgba(60, 60, 60, 128);

    private boolean dragging = false;
    private double dragX, dragY;
    private int prevDX, prevDY;

    public GuiOverlayDialog(IGuiWrapper gui, int x, int y, int width, int height) {
       super(GuiMekanism.BASE_BACKGROUND, gui, x, y, width, height);
       active = true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // drag 'safe area'
        if (isMouseOver(mouseX, mouseY)) {
            if (mouseY < y + 15) {
                dragging = true;
                dragX = mouseX; dragY = mouseY;
                prevDX = 0; prevDY = 0;
            }
            super.mouseClicked(mouseX, mouseY, button);
        } else {
            if (guiObj instanceof GuiMekanism && ((GuiMekanism<?>) guiObj).getContainer() != null) {
                Container c = ((GuiMekanism<?>) guiObj).getContainer();
                if (!(c instanceof IEmptyContainer)) {
                    if (mouseX >= guiObj.getLeft() && mouseX < guiObj.getLeft() + guiObj.getWidth() && mouseY >= guiObj.getTop() + guiObj.getHeight() - 90) {
                        return false;
                    }
                }
            }
        }
        // always return true to prevent background clicking
        return true;
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double mouseXOld, double mouseYOld) {
        if (dragging) {
            int newDX = (int) Math.round(mouseX - dragX), newDY = (int) Math.round(mouseY - dragY);
            int changeX = Math.max(-x, Math.min(minecraft.getMainWindow().getScaledWidth() - (x + width), newDX - prevDX));
            int changeY = Math.max(-y, Math.min(minecraft.getMainWindow().getScaledHeight() - (y + height), newDY - prevDY));
            prevDX = newDX; prevDY = newDY;
            move(changeX, changeY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        dragging = false;
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    protected void close() {
        children.forEach(child -> child.onWindowClose());
        guiObj.removeElement(this);
        if (guiObj instanceof GuiMekanism) {
            ((GuiMekanism<?>) guiObj).setFocused(null);
        }
    }

    protected boolean renderOverlay() {
        return true;
    }
}
