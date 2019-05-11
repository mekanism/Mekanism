package mekanism.client.render.tileentity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntityGasTank;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGasTank extends TileEntitySpecialRenderer<TileEntityGasTank> {

    @Override
    public void render(TileEntityGasTank tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderer.machineRenderer.render(tileEntity, x, y, z, partialTick, destroyStage, alpha);
    }
}