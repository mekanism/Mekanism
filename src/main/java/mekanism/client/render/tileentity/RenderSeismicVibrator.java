package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> {

    private ModelSeismicVibrator model = new ModelSeismicVibrator();

    @Override
    public void func_225616_a_(@Nonnull TileEntitySeismicVibrator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "seismic_vibrator.png"));
        MekanismRenderer.rotate(tile.getDirection(), 0, 180, 90, 270);
        RenderSystem.rotatef(180, 0, 0, 1);
        float actualRate = (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F);
        model.renderWithPiston(Math.max(0, actualRate), 0.0625F);
        RenderSystem.popMatrix();*/
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySeismicVibrator tile) {
        return true;
    }
}