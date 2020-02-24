package mekanism.client.gui.element.slot;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.inventory.container.slot.SlotOverlay;

public class GuiSlot extends GuiTexturedElement {

    private SlotOverlay overlay = null;

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        active = false;
    }

    public GuiSlot with(SlotOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
        if (overlay != null) {
            minecraft.textureManager.bindTexture(overlay.getTexture());
            blit(x, y, 0, 0, overlay.getWidth(), overlay.getHeight(), overlay.getWidth(), overlay.getHeight());
        }
    }
}