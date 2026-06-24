package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

public class FilterSelectButton extends MekanismButton {

    private static final Identifier UP_ARROW = Mekanism.rl("button/filter_arrow/up");
    private static final Identifier UP_ARROW_HOVERED = Mekanism.rl("button/filter_arrow/up_hovered");
    private static final Identifier DOWN_ARROW = Mekanism.rl("button/filter_arrow/down");
    private static final Identifier DOWN_ARROW_HOVERED = Mekanism.rl("button/filter_arrow/down_hovered");

    private final Identifier texture;
    private final Identifier hoveredTexture;
    private final boolean down;

    public FilterSelectButton(IGuiWrapper gui, int x, int y, boolean down, IClickable onPress) {
        super(gui, x, y, 11, 7, CommonComponents.EMPTY, onPress);
        this.down = down;
        this.texture = this.down ? DOWN_ARROW : UP_ARROW;
        this.hoveredTexture = this.down ? DOWN_ARROW_HOVERED : UP_ARROW_HOVERED;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Identifier sprite = isMouseOverCheckWindows(mouseX, mouseY) ? hoveredTexture : texture;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
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