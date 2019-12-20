package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderAdvancedSolarGenerator extends MekanismTileEntityRenderer<TileEntityAdvancedSolarGenerator> {

    private static final ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    @Override
    public void func_225616_a_(@Nonnull TileEntityAdvancedSolarGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5F, 1.5F, 0.5F);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        model.render(matrix, renderer, light);
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityAdvancedSolarGenerator tile) {
        return true;
    }
}