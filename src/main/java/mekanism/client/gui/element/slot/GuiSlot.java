package mekanism.client.gui.element.slot;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.inventory.container.slot.SlotOverlay;

public class GuiSlot extends GuiTexturedElement {

    private static final int DEFAULT_SLOT_COLOR = -2130706433;
    private SlotOverlay overlay = null;
    private boolean renderHover = false;

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        active = false;
    }

    public GuiSlot with(SlotOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    public GuiSlot setRenderHover(boolean renderHover) {
        this.renderHover = renderHover;
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

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        if (renderHover && isHovered()) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            int xPos = relativeX + 1, yPos = relativeY + 1;
            fillGradient(xPos, yPos, xPos + 16, yPos + 16, DEFAULT_SLOT_COLOR, DEFAULT_SLOT_COLOR);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
        }
    }
}