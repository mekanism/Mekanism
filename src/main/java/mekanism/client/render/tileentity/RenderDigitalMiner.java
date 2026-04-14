package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.LazyModel;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.tileentity.RenderDigitalMiner.DigitalMinerRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderDigitalMiner extends MekanismTileEntityRenderer<TileEntityDigitalMiner, DigitalMinerRenderState> {

    private static final LazyModel model = new LazyModel(() -> new Model3D()
          .setTexture(MekanismRenderer.whiteIcon)
          .bounds(0, 1)
    );
    private static final int[] colors = new int[EnumUtils.DIRECTIONS.length];

    static {
        colors[Direction.UP.ordinal()] = colors[Direction.DOWN.ordinal()] = ARGB.white(0.82F);
        colors[Direction.SOUTH.ordinal()] = colors[Direction.NORTH.ordinal()] = ARGB.white(0.8F);
        colors[Direction.EAST.ordinal()] = colors[Direction.WEST.ordinal()] = ARGB.white(0.78F);
    }

    public static void resetCachedVisuals() {
        model.reset();
    }

    public RenderDigitalMiner(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DigitalMinerRenderState createRenderState() {
        return new DigitalMinerRenderState();
    }

    @Override
    public void extractRenderState(TileEntityDigitalMiner miner, DigitalMinerRenderState state, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(miner, state, partialTick, cameraPosition, breakProgress);
        state.minY = miner.getMinY();
        state.maxY = miner.getMaxY();
        state.radius = miner.getRadius();
        state.diameter = miner.getDiameter();
    }

    @Override
    public void submit(DigitalMinerRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        //Adjust translation and scale ever so slightly so that no z-fighting happens at the edges if there are blocks there
        poseStack.translate(-state.radius + 0.01, state.minY - state.blockPos.getY() + 0.01, -state.radius + 0.01);
        float diameter = state.diameter - 0.02F;
        poseStack.scale(diameter, state.maxY - state.minY - 0.02F, diameter);
        Vec3 camPos = camera.pos;
        //If we are inside the visualization we don't have to render the "front" face, otherwise we need to render both given how the visualization works
        // we want to be able to see all faces easily
        FaceDisplay faceDisplay = isInsideBounds(camPos,
              state.blockPos.getX() - state.radius, state.minY, state.blockPos.getZ() - state.radius,
              state.blockPos.getX() + state.radius + 1, state.maxY, state.blockPos.getZ() + state.radius + 1
        ) ? FaceDisplay.BACK : FaceDisplay.BOTH;
        MekanismRenderer.renderObject(model.get(), poseStack, Sheets.translucentCullBlockSheet(), colors, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
              faceDisplay, camPos);
        poseStack.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.DIGITAL_MINER;
    }

    @Override
    public boolean shouldRenderOffScreen() {
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

    public static class DigitalMinerRenderState extends BlockEntityRenderState {

        public int minY;
        public int maxY;
        public int radius;
        public int diameter;
    }
}