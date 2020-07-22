package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiInsetElement<TILE extends TileEntity> extends GuiSideHolder {

    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final TILE tile;
    protected final ResourceLocation overlay;

    public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, TILE tile, int x, int y, int height, int innerSize, boolean left) {
        super(gui, x, y, height, left);
        this.overlay = overlay;
        this.tile = tile;
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerWidth) / 2;
        playClickSound = true;
        active = true;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + border && xAxis < x + width - border && yAxis >= y + border && yAxis < y + height - border;
    }

    @Override
    protected int getButtonX() {
        return x + border + (left ? 1 : -1);
    }

    @Override
    protected int getButtonY() {
        return y + border;
    }

    @Override
    protected int getButtonWidth() {
        return innerWidth;
    }

    @Override
    protected int getButtonHeight() {
        return innerHeight;
    }

    protected ResourceLocation getOverlay() {
        return overlay;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw the button background
        drawButton(matrix, mouseX, mouseY);
        //Draw the overlay onto the button
        minecraft.textureManager.bindTexture(getOverlay());
        blit(matrix, getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
    }
}