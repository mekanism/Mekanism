package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.LazyModel;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner> {

    private static final LazyModel model = new LazyModel(() -> new Model3D()
          .setTexture(MekanismRenderer.whiteIcon)
          .bounds(0, 1)
    );
    private static final int[] colors = new int[EnumUtils.DIRECTIONS.length];

    static {
        colors[Direction.UP.ordinal()] = colors[Direction.DOWN.ordinal()] = MekanismRenderer.getColorARGB(0xFFFFFF, 0.82F);
        colors[Direction.SOUTH.ordinal()] = colors[Direction.NORTH.ordinal()] = MekanismRenderer.getColorARGB(0xFFFFFF, 0.8F);
        colors[Direction.EAST.ordinal()] = colors[Direction.WEST.ordinal()] = MekanismRenderer.getColorARGB(0xFFFFFF, 0.78F);
    }

    public static void resetCachedVisuals() {
        model.reset();
    }

    public RenderDigitalMiner(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityDigitalMiner miner, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        matrix.pushPose();
        //Adjust translation and scale ever so slightly so that no z-fighting happens at the edges if there are blocks there
        matrix.translate(-miner.getRadius() + 0.01, miner.getMinY() - miner.getBlockPos().getY() + 0.01, -miner.getRadius() + 0.01);
        float diameter = miner.getDiameter() - 0.02F;
        matrix.scale(diameter, miner.getMaxY() - miner.getMinY() - 0.02F, diameter);
        //If we are inside the visualization we don't have to render the "front" face, otherwise we need to render both given how the visualization works
        // we want to be able to see all faces easily
        FaceDisplay faceDisplay = isInsideBounds(miner.getBlockPos().getX() - miner.getRadius(), miner.getMinY(), miner.getBlockPos().getZ() - miner.getRadius(),
              miner.getBlockPos().getX() + miner.getRadius() + 1, miner.getMaxY(), miner.getBlockPos().getZ() + miner.getRadius() + 1)
                                  ? FaceDisplay.BACK : FaceDisplay.BOTH;
        MekanismRenderer.renderObject(model.get(), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), colors, LightTexture.FULL_BRIGHT, overlayLight,
              faceDisplay, getCamera());
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DIGITAL_MINER;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityDigitalMiner tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityDigitalMiner tile, Vec3 camera) {
        return tile.isClientRendering() && tile.canDisplayVisuals() && super.shouldRender(tile, camera);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityDigitalMiner tile) {
        if (tile.isClientRendering() && tile.canDisplayVisuals()) {
            BlockPos pos = tile.getBlockPos();
            int radius = tile.getRadius();
            return new AABB(
                  pos.getX() - radius,
                  tile.getMinY(),
                  pos.getZ() - radius,
                  pos.getX() + radius + 1,
                  tile.getMaxY() + 1,
                  pos.getZ() + radius + 1
            );
        }
        return super.getRenderBoundingBox(tile);
    }
}