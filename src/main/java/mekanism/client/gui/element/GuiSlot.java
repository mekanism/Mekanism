package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiSlot extends GuiElement {

    private final int textureX;
    private final int textureY;

    private SlotOverlay overlay = null;

    public GuiSlot(SlotType type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "slot.png"), gui, def, x, y, type.width, type.height);
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
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(x, y, textureX, textureY, width, height);
        if (overlay != null) {
            guiObj.drawTexturedRect(x + (width - overlay.width) / 2, y + (height - overlay.height) / 2, overlay.textureX, overlay.textureY, overlay.width, overlay.height);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    public enum SlotType {
        NORMAL(18, 18, 0, 0),
        POWER(18, 18, 18, 0),
        INPUT(18, 18, 36, 0),
        EXTRA(18, 18, 54, 0),
        OUTPUT(18, 18, 72, 0),
        OUTPUT_LARGE(26, 26, 90, 0),
        OUTPUT_WIDE(42, 26, 116, 0);

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

    public enum SlotOverlay {
        MINUS(18, 18, 0, 18),
        PLUS(18, 18, 18, 18),
        POWER(18, 18, 36, 18),
        INPUT(18, 18, 54, 18),
        OUTPUT(18, 18, 72, 18),
        CHECK(18, 18, 0, 36);

        public final int width;
        public final int height;

        public final int textureX;
        public final int textureY;

        SlotOverlay(int w, int h, int x, int y) {
            width = w;
            height = h;

            textureX = x;
            textureY = y;
        }
    }
}