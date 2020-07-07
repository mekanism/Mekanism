package mekanism.generators.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.generators.client.model.ModelWindGenerator;
import mekanism.generators.common.GeneratorsProfilerConstants;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderWindGenerator extends MekanismTileEntityRenderer<TileEntityWindGenerator> implements IWireFrameRenderer {

    private final ModelWindGenerator model = new ModelWindGenerator();

    public RenderWindGenerator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityWindGenerator tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        double angle = performTranslationsAndGetAngle(tile, partialTick, matrix);
        model.render(matrix, renderer, angle, light, overlayLight, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return GeneratorsProfilerConstants.WIND_GENERATOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityWindGenerator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntityWindGenerator) {
            double angle = performTranslationsAndGetAngle((TileEntityWindGenerator) tile, partialTick, matrix);
            model.renderWireFrame(matrix, buffer, angle, red, green, blue, alpha);
            matrix.pop();
        }
    }

    /**
     * Make sure to call matrix.pop afterwards
     */
    private double performTranslationsAndGetAngle(TileEntityWindGenerator tile, float partialTick, MatrixStack matrix) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        double angle = tile.getAngle();
        if (tile.getActive()) {
            angle = (tile.getAngle() + ((tile.getPos().getY() + 4F) / TileEntityWindGenerator.SPEED_SCALED) * partialTick) % 360;
        }
        return angle;
    }
}