package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiTileEntityElement<TILE extends TileEntity> extends GuiTexturedElement {

    protected final TILE tileEntity;

    public GuiTileEntityElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile) {
        this(resource, gui, def, tile, 0, 0);
    }

    public GuiTileEntityElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y) {
        this(resource, gui, def, tile, x, y, 0, 0);
    }

    public GuiTileEntityElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int x, int y, int width, int height) {
        super(resource, gui, def, x, y, width, height);
        this.tileEntity = tile;
    }
}