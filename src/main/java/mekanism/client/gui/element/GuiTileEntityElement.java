package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public abstract class GuiTileEntityElement<TILE extends TileEntity> extends GuiTexturedElement {

    protected final TILE tile;

    public GuiTileEntityElement(ResourceLocation resource, IGuiWrapper gui, TILE tile, int x, int y, int width, int height) {
        super(resource, gui, x, y, width, height);
        this.tile = tile;
    }
}