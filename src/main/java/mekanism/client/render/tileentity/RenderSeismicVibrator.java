package mekanism.client.render.tileentity;

import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSeismicVibrator extends TileEntitySpecialRenderer<TileEntitySeismicVibrator> {

    private ModelSeismicVibrator model = new ModelSeismicVibrator();

    @Override
    public void render(TileEntitySeismicVibrator tileEntity, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translate(x + 0.5, y + 1.5, z + 0.5);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SeismicVibrator.png"));

        MekanismRenderer.glRotateForFacing(tileEntity);

        float actualRate = (float) Math.sin((tileEntity.clientPiston + (tileEntity.isActive ? partialTick : 0)) / 5F);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        model.renderWithPiston(Math.max(0, actualRate), 0.0625F);
        renderHelper.cleanup();
    }
}