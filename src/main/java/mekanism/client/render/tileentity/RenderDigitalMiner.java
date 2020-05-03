package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MinerVisualRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.TileEntity;

@ParametersAreNonnullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> implements IWireFrameRenderer {

    private final ModelDigitalMiner model = new ModelDigitalMiner();

    public RenderDigitalMiner(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityDigitalMiner tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        performTranslations(tile, matrix);
        model.render(matrix, renderer, light, overlayLight, tile.getActive());
        matrix.pop();
        if (tile.clientRendering) {
            profiler.startSection(ProfilerConstants.DIGITAL_MINER_VISUALS);
            MinerVisualRenderer.render(tile, matrix, renderer);
            profiler.endSection();
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DIGITAL_MINER;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityDigitalMiner tile) {
        return true;
    }

    @Override
    public void renderWireFrame(TileEntity tile, float partialTick, MatrixStack matrix, IVertexBuilder buffer, float red, float green, float blue, float alpha) {
        if (tile instanceof TileEntityDigitalMiner) {
            performTranslations((TileEntityDigitalMiner) tile, matrix);
            model.renderWireFrame(matrix, buffer, red, green, blue, alpha);
            matrix.pop();
        }
    }

    /**
     * Make sure to call matrix.pop afterwards
     */
    private void performTranslations(TileEntityDigitalMiner tile, MatrixStack matrix) {
        matrix.push();
        matrix.translate(0.5, 1.5, 0.5);
        MekanismRenderer.rotate(matrix, tile.getDirection(), 0, 180, 90, 270);
        matrix.translate(0, 0, -1);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
    }
}