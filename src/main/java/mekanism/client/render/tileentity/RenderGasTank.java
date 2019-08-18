package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderGasTank extends TileEntityRenderer<TileEntityGasTank> {

    @Override
    public void render(TileEntityGasTank tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage);
    }
}