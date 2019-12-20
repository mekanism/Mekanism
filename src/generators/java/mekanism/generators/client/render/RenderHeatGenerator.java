package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelHeatGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;

public class RenderHeatGenerator extends MekanismTileEntityRenderer<TileEntityHeatGenerator> {

    private static final ModelHeatGenerator model = new ModelHeatGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityHeatGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5F, 1.5F, 0.5F);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 180, 0, 270, 90);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, tile.getActive());
        matrix.func_227865_b_();
    }
}