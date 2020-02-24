package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiInsetElement<TILE extends TileEntity> extends GuiSideHolder {

    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final TILE tile;
    protected final ResourceLocation overlay;

    //TODO: Improve the overlays by having some spots have alpha for transparency rather than only keeping the "key" parts. Do the same for the MekanismImageButtons
    public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, TILE tile, int x, int y, int height, int innerSize) {
        super(gui, x, y, height);
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
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        //Draw the button background
        drawButton(mouseX, mouseY);
        //Draw the overlay onto the button
        minecraft.textureManager.bindTexture(overlay);
        blit(getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
    }
}