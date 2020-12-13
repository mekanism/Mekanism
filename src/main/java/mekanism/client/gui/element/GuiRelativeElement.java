package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class GuiRelativeElement extends GuiElement {

    protected int relativeX;
    protected int relativeY;

    public GuiRelativeElement(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, gui.getLeft() + x, gui.getTop() + y, width, height, StringTextComponent.EMPTY);
        this.relativeX = x;
        this.relativeY = y;
    }

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    @Override
    public void move(int changeX, int changeY) {
        super.move(changeX, changeY);
        //Note: When moving we need to adjust our relative position
        // but when resizing, we don't as we are relative to the
        // positions changing when resizing, instead of moving
        // where we are in relation to
        relativeX += changeX;
        relativeY += changeY;
    }

    @Override
    public void drawCenteredTextScaledBound(MatrixStack matrix, ITextComponent text, float maxLength, float y, int color) {
        float scale = Math.min(1, maxLength / getStringWidth(text));
        drawScaledCenteredText(matrix, text, relativeX + getXSize() / 2F, relativeY + y, color, scale);
    }
}