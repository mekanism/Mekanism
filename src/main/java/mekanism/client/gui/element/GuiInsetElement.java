package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Better name?
@OnlyIn(Dist.CLIENT)
public abstract class GuiInsetElement<TILE extends TileEntity> extends GuiTileEntityElement<TILE> {

    public static final ResourceLocation INSET_HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inset_holder_left.png");
    public static final ResourceLocation INSET_HOLDER_RIGHT = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "inset_holder_right.png");
    protected final int border;
    protected final int innerWidth;
    protected final int innerHeight;
    protected final boolean left;

    //TODO: Improve the overlays by having some spots have alpha for transparency rather than only keeping the "key" parts. Do the same for the MekanismImageButtons
    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int size, int innerSize) {
        this(resource, gui, def, tile, x, y, size, size, innerSize);
    }

    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int width, int height, int innerSize) {
        super(resource, gui, def, tile, x, y, width, height);
        this.innerWidth = innerSize;
        this.innerHeight = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerWidth) / 2;
        this.left = x < 0;
    }

    protected ResourceLocation getHolderTexture() {
        return left ? INSET_HOLDER_LEFT : INSET_HOLDER_RIGHT;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + border && xAxis < x + width - border && yAxis >= y + border && yAxis < y + height - border;
    }

    protected void colorTab() {
        //Don't do any coloring by default
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

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getHolderTexture());
        colorTab();
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        MekanismRenderer.resetColor();

        //Draw the button background
        super.renderButton(mouseX, mouseY, partialTicks);

        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(getButtonX(), getButtonY(), 0, 0, innerWidth, innerHeight, innerWidth, innerHeight);
        minecraft.textureManager.bindTexture(defaultLocation);
    }
}