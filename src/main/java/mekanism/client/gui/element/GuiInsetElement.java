package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Better name?
@OnlyIn(Dist.CLIENT)
public abstract class GuiInsetElement<TILE extends TileEntity> extends GuiTileEntityElement<TILE> {

    protected final int border;
    protected final int innerSize;
    private final int offset;

    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int size, int innerSize) {
        this(resource, gui, def, tile, x, y, size, size, innerSize);
    }

    public GuiInsetElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int width, int height, int innerSize) {
        super(resource, gui, def, tile, x, y, width, height);
        this.innerSize = innerSize;
        //TODO: decide what to do if this doesn't divide nicely
        this.border = (width - innerSize) / 2;
        //TODO: Figure out why this shift is needed to make it render properly
        offset = x < 0 ? 1 : -1;
    }

    protected int getYOffset(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY) ? 0 : innerSize;
    }

    protected int getXOffset() {
        return width;
    }

    @Override
    public boolean isMouseOver(double xAxis, double yAxis) {
        //TODO: override isHovered
        return this.active && this.visible && xAxis >= x + border && xAxis < x + width - border && yAxis >= y + border && yAxis < y + height - border;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(x, y, 0, 0, width, height);
        guiObj.drawTexturedRect(x + border + offset, y + border, getXOffset(), getYOffset(mouseX, mouseY), innerSize, innerSize);
        minecraft.textureManager.bindTexture(defaultLocation);
    }
}