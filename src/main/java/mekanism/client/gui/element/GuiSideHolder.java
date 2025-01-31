package mekanism.client.gui.element;

import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiSideHolder extends GuiTexturedElement {

    public static GuiSideHolder armorHolder(IGuiWrapper gui) {
        return create(gui, -26, 62, 98, true, true, SpecialColors.TAB_ARMOR_SLOTS);
    }

    public static GuiSideHolder create(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder, ColorRegistryObject tabColor) {
        return new GuiSideHolder(gui, x, y, height, left, slotHolder) {
            @Override
            protected void colorTab(GuiGraphics guiGraphics) {
                MekanismRenderer.color(guiGraphics, tabColor);
            }
        };
    }

    private static final ResourceLocation HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI, "holder_left.png");
    private static final ResourceLocation HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI, "holder_right.png");
    private static final int TEXTURE_WIDTH = 26;
    private static final int TEXTURE_HEIGHT = 9;

    protected final boolean left;
    private final boolean slotHolder;

    protected GuiSideHolder(IGuiWrapper gui, int x, int y, int height, boolean left, boolean slotHolder) {
        super(left ? HOLDER_LEFT : HOLDER_RIGHT, gui, x, y, TEXTURE_WIDTH, height);
        this.left = left;
        this.slotHolder = slotHolder;
        active = false;
        if (!this.slotHolder) {
            setButtonBackground(ButtonBackground.DEFAULT);
        }
    }

    protected abstract void colorTab(GuiGraphics guiGraphics);

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        if (this.slotHolder) {
            //Slot holders need to draw here to render behind the slots instead of in front of them
            draw(guiGraphics);
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (!this.slotHolder) {
            draw(guiGraphics);
        }
    }

    protected void draw(@NotNull GuiGraphics guiGraphics) {
        colorTab(guiGraphics);
        drawUncolored(guiGraphics);
        MekanismRenderer.resetColor(guiGraphics);
    }

    protected void drawUncolored(@NotNull GuiGraphics guiGraphics) {
        GuiUtils.blitNineSlicedSized(guiGraphics, getResource(), relativeX, relativeY, width, height, 4, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}