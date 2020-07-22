package mekanism.client.gui.element.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class FilterSelectButton extends MekanismButton {

    private static final ResourceLocation ARROWS = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "filter_arrows.png");
    private static final int TEXTURE_WIDTH = 22;
    private static final int TEXTURE_HEIGHT = 14;

    private final boolean down;

    public FilterSelectButton(IGuiWrapper gui, int x, int y, boolean down, Runnable onPress, IHoverable onHover) {
        super(gui, x, y, 11, 7, StringTextComponent.EMPTY, onPress, onHover);
        this.down = down;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (resetColorBeforeRender()) {
            MekanismRenderer.resetColor();
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        int width = getButtonWidth();
        int height = getButtonHeight();
        int x = getButtonX();
        int y = getButtonY();
        MekanismRenderer.bindTexture(ARROWS);
        blit(matrix, x, y, isMouseOverCheckWindows(mouseX, mouseY) ? width : 0, down ? 7 : 0, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        if (super.isMouseOver(xAxis, yAxis)) {
            //First we do a basic check to see if we are over the button if it was a rectangle rather than a triangle.
            double xShifted = xAxis - x;
            double yShifted = yAxis - y;
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