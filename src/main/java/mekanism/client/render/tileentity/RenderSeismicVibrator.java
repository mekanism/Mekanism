package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;

public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> {

    private ModelSeismicVibrator model = new ModelSeismicVibrator();

    @Override
    public void func_225616_a_(@Nonnull TileEntitySeismicVibrator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        float actualRate = (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F);
        model.render(matrix, renderer, light, otherLight, Math.max(0, actualRate));
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySeismicVibrator tile) {
        return true;
    }
}