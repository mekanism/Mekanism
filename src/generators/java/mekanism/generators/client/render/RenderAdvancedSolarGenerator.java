package mekanism.generators.client.render;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderAdvancedSolarGenerator extends MekanismTileEntityRenderer<TileEntityAdvancedSolarGenerator> {

    private final ModelAdvancedSolarGenerator model = new ModelAdvancedSolarGenerator();

    public RenderAdvancedSolarGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityAdvancedSolarGenerator tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        model.render(matrix, renderer, light, overlayLight, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.ADVANCED_SOLAR_GENERATOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityAdvancedSolarGenerator tile) {
        return true;
    }
}