package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> {

    private static final float SCALE_FIX = 0.9999F;
    private static final float OFFSET_FIX = 0.00005F;
    private static Model3D model;

    public static void resetCachedVisuals() {
        model = null;
    }

    public RenderDigitalMiner(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityDigitalMiner miner, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (miner.clientRendering && miner.getRadius() <= 64) {
            if (model == null) {
                model = new Model3D();
                model.setTexture(MekanismRenderer.whiteIcon);
                model.minX = 0.01;
                model.minY = 0.01;
                model.minZ = 0.01;
                model.maxX = 0.99;
                model.maxY = 0.99;
                model.maxZ = 0.99;
            }
            //TODO: Eventually we may want to make it so that the model can support each face being a different
            // color to make it easier to see the "depth"
            matrix.push();
            matrix.translate(-miner.getRadius(), miner.getMinY() - miner.getPos().getY(), -miner.getRadius());
            matrix.scale(miner.getDiameter(), miner.getMaxY() - miner.getMinY(), miner.getDiameter());
            //Adjust it slightly so that it does not clip into the blocks that are just on the outside of the radius
            matrix.scale(SCALE_FIX, SCALE_FIX, SCALE_FIX);
            matrix.translate(OFFSET_FIX, OFFSET_FIX, OFFSET_FIX);
            MekanismRenderer.renderObject(model, matrix, renderer.getBuffer(Atlases.getTranslucentCullBlockType()),
                  MekanismRenderer.getColorARGB(255, 255, 255, 0.8F), MekanismRenderer.FULL_LIGHT, overlayLight);
            matrix.pop();
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
}