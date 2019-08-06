package mekanism.client.render.tileentity;

import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSeismicVibrator extends TileEntitySpecialRenderer<TileEntitySeismicVibrator> {

    private ModelSeismicVibrator model = new ModelSeismicVibrator();

    @Override
    public void render(TileEntitySeismicVibrator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SeismicVibrator.png"));
        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        GlStateManager.rotate(180, 0, 0, 1);
        float actualRate = (float) Math.sin((tileEntity.clientPiston + (tileEntity.getActive() ? partialTick : 0)) / 5F);
        model.renderWithPiston(Math.max(0, actualRate), 0.0625F);
        GlStateManager.popMatrix();
    }
}