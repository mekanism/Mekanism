package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiInsetElement<DATA_SOURCE> extends GuiSideHolder {

    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final DATA_SOURCE dataSource;
    protected final ResourceLocation overlay;

    public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left) {
        super(gui, x, y, height, left, false);
        this.overlay = overlay;
        this.dataSource = dataSource;
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerWidth) / 2;
        this.clickSound = BUTTON_CLICK_SOUND;
        active = true;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= getX() + border && xAxis < getRight() - border && yAxis >= getY() + border && yAxis < getBottom() - border;
    }

    @Override
    protected int getButtonX() {
        return super.getButtonX() + border + (left ? 1 : -1);
    }

    @Override
    protected int getButtonY() {
        return super.getButtonY() + border;
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
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the button background
        if (buttonBackground != ButtonBackground.NONE) {
            //Validate the background didn't get set to none by a child
            drawButton(guiGraphics, mouseX, mouseY);
        }
        drawBackgroundOverlay(guiGraphics);
    }

    protected void drawBackgroundOverlay(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.blit(getOverlay(), getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
    }
}