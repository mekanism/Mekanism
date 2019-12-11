package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class RenderGasTank extends TileEntityRenderer<TileEntityGasTank> {

    @Override
    public void render(TileEntityGasTank tile, double x, double y, double z, float partialTick, int destroyStage) {
        MekanismRenderer.machineRenderer().render(tile, x, y, z, partialTick, destroyStage);
    }
}