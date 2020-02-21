package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;

public class GuiSlot extends GuiTexturedElement {

    private final int textureX;
    private final int textureY;

    private SlotOverlay overlay = null;

    public GuiSlot(SlotType type, IGuiWrapper gui, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "slot.png"), gui, x, y, type.width, type.height);
        textureX = type.textureX;
        textureY = type.textureY;
        //TODO: Mark any other ones as inactive that shouldn't allow clicking
        //Mark the slot as inactive as we really just want to be drawing it and not intercepting the mouse clicks
        active = false;
    }

    public GuiSlot with(SlotOverlay overlay) {
        this.overlay = overlay;
        return this;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawTexturedRect(x, y, textureX, textureY, width, height);
        if (overlay != null) {
            guiObj.drawTexturedRect(x + (width - overlay.width) / 2, y + (height - overlay.height) / 2, overlay.textureX, overlay.textureY, overlay.width, overlay.height);
        }
    }

    public enum SlotType {
        NORMAL(18, 18, 0, 0),
        POWER(18, 18, 18, 0),
        INPUT(18, 18, 36, 0),
        EXTRA(18, 18, 54, 0),
        OUTPUT(18, 18, 72, 0),
        OUTPUT_WIDE(42, 26, 90, 0);

        public final int width;
        public final int height;

        public final int textureX;
        public final int textureY;

        SlotType(int w, int h, int x, int y) {
            width = w;
            height = h;

            textureX = x;
            textureY = y;
        }
    }

}