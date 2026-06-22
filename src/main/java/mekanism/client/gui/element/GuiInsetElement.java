package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderPipelines;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class GuiInsetElement<DATA_SOURCE extends @Nullable Object> extends GuiSideHolder implements ISupportsWarning<GuiInsetElement<DATA_SOURCE>> {

    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final DATA_SOURCE dataSource;
    protected final Identifier overlay;

    @Nullable
    protected BooleanSupplier warningSupplier;

    public GuiInsetElement(Identifier overlay, IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left) {
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
    public GuiInsetElement<DATA_SOURCE> warning(WarningType type, BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
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

    protected Identifier getOverlay() {
        return overlay;
    }

    @Override
    protected void draw(GuiGraphicsExtractor guiGraphics) {
        boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
        if (warning) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, width, height);
            //Draw the warning overlay (multiply-blended)
            guiGraphics.blit(MekanismRenderPipelines.WARNING_PIPELINE, WARNING_TEXTURE, relativeX, relativeY, 0, 0, width, height, 256, 256);
        } else {
            super.draw(guiGraphics);
        }
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the button background
        if (buttonBackground != ButtonBackground.NONE) {
            //Validate the background didn't get set to none by a child
            drawButton(guiGraphics, mouseX, mouseY);
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getOverlay(), getButtonX(), getButtonY(), innerWidth, innerHeight);
    }
}