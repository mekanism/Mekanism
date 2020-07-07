package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelSeismicVibrator;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3f;

@ParametersAreNonnullByDefault
public class RenderSeismicVibrator extends MekanismTileEntityRenderer<TileEntitySeismicVibrator> implements IWireFrameRenderer {

    private final ModelSeismicVibrator model = new ModelSeismicVibrator();

    public RenderSeismicVibrator(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySeismicVibrator tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        float actualRate = performTranslationsAndGetRate(tile, partialTick, matrix);
        model.render(matrix, renderer, light, overlayLight, actualRate, false);
        matrix.pop();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SEISMIC_VIBRATOR;
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySeismicVibrator tile) {
        return true;
    }

    @Override
    public void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntitySeismicVibrator) {
            float actualRate = performTranslationsAndGetRate((TileEntitySeismicVibrator) tile, partialTick, matrix);
            model.renderWireFrame(matrix, buffer, actualRate, red, green, blue, alpha);
            matrix.pop();
        }
    }

    /**
     * Make sure to call matrix.pop afterwards
     */
    private float performTranslationsAndGetRate(TileEntitySeismicVibrator tile, float partialTick, MatrixStack matrix) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        return Math.max(0, (float) Math.sin((tile.clientPiston + (tile.getActive() ? partialTick : 0)) / 5F));
    }
}