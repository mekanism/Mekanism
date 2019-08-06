package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTileEntityElement<TILE extends TileEntity> extends GuiElement {

    protected final TILE tileEntity;

    public GuiTileEntityElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile) {
        super(resource, gui, def);
        this.tileEntity = tile;
    }
}