package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> {

    private static Model3D model;
    private static final int[] colors = new int[EnumUtils.DIRECTIONS.length];
    static {
        colors[Direction.DOWN.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
        colors[Direction.UP.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.82F);
        colors[Direction.NORTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.8F);
        colors[Direction.SOUTH.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.8F);
        colors[Direction.WEST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
        colors[Direction.EAST.ordinal()] = MekanismRenderer.getColorARGB(255, 255, 255, 0.78F);
    }

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
                model.minX = 0;
                model.minY = 0;
                model.minZ = 0;
                model.maxX = 1;
                model.maxY = 1;
                model.maxZ = 1;
            }
            matrix.push();
            //Adjust translation and scale ever so slightly so that no z-fighting happens at the edges if there are blocks there
            matrix.translate(-miner.getRadius() + 0.01, miner.getMinY() - miner.getPos().getY() + 0.01, -miner.getRadius() + 0.01);
            float diameter = miner.getDiameter() - 0.02F;
            matrix.scale(diameter, miner.getMaxY() - miner.getMinY() - 0.02F, diameter);
            //If we are inside of the visualization we don't have to render the "front" face, otherwise we need to render both given how the visualization works
            // we want to be able to see all faces easily
            FaceDisplay faceDisplay = isInsideBounds(miner.getPos().getX() - miner.getRadius(), miner.getMinY(), miner.getPos().getZ() - miner.getRadius(),
                  miner.getPos().getX() + miner.getRadius(), miner.getMaxY(), miner.getPos().getZ() + miner.getRadius()) ? FaceDisplay.BACK : FaceDisplay.BOTH;
            MekanismRenderer.renderObject(model, matrix, renderer.getBuffer(Atlases.getTranslucentCullBlockType()), colors, MekanismRenderer.FULL_LIGHT, overlayLight,
                  faceDisplay);
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