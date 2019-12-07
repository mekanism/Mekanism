package mekanism.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSeismicVibrator extends TileEntityRenderer<TileEntitySeismicVibrator> {

    private ModelSeismicVibrator model = new ModelSeismicVibrator();

    @Override
    public void render(TileEntitySeismicVibrator tileEntity, double x, double y, double z, float partialTick, int destroyStage) {
        GlStateManager.pushMatrix();
        //Move origin to 0.5, 1.5, 0.5 offset from where we currently are
        GlStateManager.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "seismic_vibrator.png"));
        //Rotate so we are facing the correct y direction??
        MekanismRenderer.rotate(tileEntity.getDirection(), 0, 180, 90, 270);
        //Rotate 180 in z to fix orientation
        GlStateManager.rotatef(180, 0, 0, 1);
        float actualRate = (float) Math.sin((tileEntity.clientPiston + (tileEntity.getActive() ? partialTick : 0)) / 5F);
        /*
        //Move origin to
        GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
        //Rotate around the Z axis
        if (this.rotateAngleZ != 0.0F) {
            GlStateManager.rotatef(this.rotateAngleZ * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F);
        }

        if (this.rotateAngleY != 0.0F) {
            GlStateManager.rotatef(this.rotateAngleY * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F);
        }

        if (this.rotateAngleX != 0.0F) {
            GlStateManager.rotatef(this.rotateAngleX * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F);
        }*/
        model.renderWithPiston(Math.max(0, actualRate), 0.0625F);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySeismicVibrator tile) {
        return true;
    }
}