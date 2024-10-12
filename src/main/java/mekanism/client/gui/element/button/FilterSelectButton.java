package mekanism.client.gui.element.button;

import com.mojang.blaze3d.systems.RenderSystem;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class FilterSelectButton extends MekanismButton {

    private static final ResourceLocation ARROWS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_arrows.png");
    private static final int TEXTURE_WIDTH = 22;
    private static final int TEXTURE_HEIGHT = 14;

    private final boolean down;

    public FilterSelectButton(IGuiWrapper gui, int x, int y, boolean down, @NotNull IClickable onPress) {
        super(gui, x, y, 11, 7, CommonComponents.EMPTY, onPress);
        this.down = down;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (resetColorBeforeRender()) {
            MekanismRenderer.resetColor(guiGraphics);
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int width = getButtonWidth();
        int height = getButtonHeight();
        int x = getButtonX();
        int y = getButtonY();
        guiGraphics.blit(ARROWS, x, y, isMouseOverCheckWindows(mouseX, mouseY) ? width : 0, down ? 7 : 0, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        if (super.isMouseOver(xAxis, yAxis)) {
            //First we do a basic check to see if we are over the button if it was a rectangle rather than a triangle.
            double xShifted = xAxis - getX();
            double yShifted = yAxis - getY();
            //Next we check it against the shapes of the different buttons
            if (down) {
                if (yShifted < 2) {
                    return true;
                } else if (yShifted < 3) {
                    return xShifted >= 1 && xShifted < 10;
                } else if (yShifted < 4) {
                    return xShifted >= 2 && xShifted < 9;
                } else if (yShifted < 5) {
                    return xShifted >= 3 && xShifted < 8;
                } else if (yShifted < 6) {
                    return xShifted >= 4 && xShifted < 7;
                }
                //else yShifted < 7
                return xShifted >= 5 && xShifted < 6;
            }
            //Up arrow
            if (yShifted < 1) {
                return xShifted >= 5 && xShifted < 6;
            } else if (yShifted < 2) {
                return xShifted >= 4 && xShifted < 7;
            } else if (yShifted < 3) {
                return xShifted >= 3 && xShifted < 8;
            } else if (yShifted < 4) {
                return xShifted >= 2 && xShifted < 9;
            } else if (yShifted < 5) {
                return xShifted >= 1 && xShifted < 10;
            }
            //else yShifted < 7
            return true;
        }
        return false;
    }
}