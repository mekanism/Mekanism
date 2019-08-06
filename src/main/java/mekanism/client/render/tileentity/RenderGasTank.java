package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.gas_tank.TileEntityGasTank;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderGasTank extends TileEntitySpecialRenderer<TileEntityGasTank> {

    @Override
    public void render(TileEntityGasTank tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderer.machineRenderer().render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}