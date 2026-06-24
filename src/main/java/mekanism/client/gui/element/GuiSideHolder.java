package mekanism.client.gui.element;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public abstract class GuiSideHolder extends GuiElement {

    public static GuiSideHolder armorHolder(IGuiWrapper gui) {
        return create(gui, -26, 62, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS);
    }

    public static GuiSideHolder create(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder, ColorRegistryObject tabColor) {
        return new GuiSideHolder(gui, x, y, height, left, slotHolder) {
            @Override
            protected int getTabColor() {
                return tabColor.argb();
            }
        };
    }

    private static final Identifier HOLDER_LEFT = Mekanism.rl("holder_left");
    private static final Identifier HOLDER_RIGHT = Mekanism.rl("holder_right");

    protected final boolean left;
    private final boolean slotHolder;

    protected GuiSideHolder(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder) {
        super(gui, x, y, 26, height);
        this.left = left;
        this.slotHolder = slotHolder;
        active = false;
        if (!this.slotHolder) {
            setButtonBackground(ButtonBackground.DEFAULT);
        }
    }

    protected abstract int getTabColor();

    @Override
    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.slotHolder) {
            //Slot holders need to draw here to render behind the slots instead of in front of them
            draw(guiGraphics);
        }
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (!this.slotHolder) {
            draw(guiGraphics);
        }
    }

    protected final Identifier getResource() {
        return left ? HOLDER_LEFT : HOLDER_RIGHT;
    }

    protected void draw(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, width, height, getTabColor());
    }

}