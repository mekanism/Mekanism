package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderAdvancedSolarGenerator extends TileEntityRenderer<TileEntityAdvancedSolarGenerator> {

    private static final ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    public RenderAdvancedSolarGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void render(@Nonnull TileEntityAdvancedSolarGenerator tile, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int overlayLight) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.field_229183_f_.func_229187_a_(180));
        model.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityAdvancedSolarGenerator tile) {
        return true;
    }
}