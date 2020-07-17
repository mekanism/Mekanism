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
    public void drawCenteredTextScaledBound(MatrixStack matrix, ITextComponent text, float maxLength, float y, int color) {
        float scale = Math.min(1, maxLength / getStringWidth(text));
        drawScaledCenteredText(matrix, text, relativeX + getXSize() / 2F, relativeY + y, color, scale);
    }
}