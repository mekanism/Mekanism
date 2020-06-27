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

    //TODO: Improve the overlays by having some spots have alpha for transparency rather than only keeping the "key" parts. Do the same for the MekanismImageButtons
    public GuiInsetElement(ResourceLocation overlay, IGuiWrapper gui, TILE tile, int x, int y, int height, int innerSize, boolean left) {
        super(gui, x, y, height, left);
        this.overlay = overlay;
        this.tile = tile;
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (field_230688_j_ - innerWidth) / 2;
        playClickSound = true;
        field_230693_o_ = true;
    }

    @Override
    public boolean func_231047_b_(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.field_230693_o_ && this.field_230694_p_ && xAxis >= field_230690_l_ + border && xAxis < field_230690_l_ + field_230688_j_ - border && yAxis >= field_230691_m_ + border && yAxis < field_230691_m_ + field_230689_k_ - border;
    }

    @Override
    protected int getButtonX() {
        return field_230690_l_ + border + (left ? 1 : -1);
    }

    @Override
    protected int getButtonY() {
        return field_230691_m_ + border;
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
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.func_230431_b_(matrix, mouseX, mouseY, partialTicks);
        //Draw the button background
        drawButton(matrix, mouseX, mouseY);
        //Draw the overlay onto the button
        minecraft.textureManager.bindTexture(getOverlay());
        func_238463_a_(matrix, getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
    }
}