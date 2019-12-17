package mekanism.client.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

public abstract class MekanismTileEntityRenderer<TILE extends TileEntity> extends TileEntityRenderer<TILE> {

    public MekanismTileEntityRenderer() {
        super(TileEntityRendererDispatcher.instance);
    }
}