package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSolarNeutronActivator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.TileEntitySolarNeutronActivator;
import net.minecraft.client.renderer.IRenderTypeBuffer;

public class RenderSolarNeutronActivator extends MekanismTileEntityRenderer<TileEntitySolarNeutronActivator> {

    private ModelSolarNeutronActivator model = new ModelSolarNeutronActivator();

    @Override
    public void func_225616_a_(@Nonnull TileEntitySolarNeutronActivator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int otherLight) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        model.render(matrix, renderer, light, otherLight);
        matrix.func_227865_b_();
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySolarNeutronActivator tile) {
        return true;
    }
}