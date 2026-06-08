package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.RenderTeleporter.TeleporterRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.CommonColors;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter, TeleporterRenderState> {

    public static final float MIN_SIDE_BOUND1 = 0.46F;
    public static final float MIN_SIDE_BOUND2 = 0;
    public static final float MAX_SIDE_BOUND1 = 0.54F;
    public static final float MAX_SIDE_BOUND2 = 1;

    public RenderTeleporter(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public TeleporterRenderState createRenderState() {
        return new TeleporterRenderState();
    }

    @Override
    public void extractRenderState(TileEntityTeleporter teleporter, TeleporterRenderState state, float partialTick, Vec3 cameraPosition,
          @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(teleporter, state, partialTick, cameraPosition, breakProgress);
        state.tint = MekanismRenderer.getColorARGB(teleporter.getColor(), 0.75F);

        Direction direction = Objects.requireNonNullElse(teleporter.frameDirection(), Direction.UP);
        boolean rotated = teleporter.frameRotated();

        if (direction.getAxis().isHorizontal()) {
            state.renderAxis = RenderResizableCuboid.SideRender.Y_AXIS;
        } else if (rotated) {
            state.renderAxis = RenderResizableCuboid.SideRender.X_AXIS;
        } else {
            state.renderAxis = RenderResizableCuboid.SideRender.Z_AXIS;
        }

        int min = direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -2;
        int max = direction.getAxisDirection() == AxisDirection.POSITIVE ? 3 : 0;

        setBounds(state, direction, rotated, min, max);
    }

    /// This is ugly, but is the only way to do it without capturing lambdas
    private static void setBounds(TeleporterRenderState state, Direction direction, boolean rotated, int min, int max) {
        switch (direction.getAxis()) {
            case X -> {
                if (rotated) {
                    state.minY = MIN_SIDE_BOUND1;
                    state.maxY = MAX_SIDE_BOUND1;
                    state.minZ = MIN_SIDE_BOUND2;
                    state.maxZ = MAX_SIDE_BOUND2;
                } else {
                    state.minZ = MIN_SIDE_BOUND1;
                    state.maxZ = MAX_SIDE_BOUND1;
                    state.minY = MIN_SIDE_BOUND2;
                    state.maxY = MAX_SIDE_BOUND2;
                }
                state.minX = min;
                state.maxX = max;
            }
            case Y -> {
                if (rotated) {
                    state.minX = MIN_SIDE_BOUND1;
                    state.maxX = MAX_SIDE_BOUND1;
                    state.minZ = MIN_SIDE_BOUND2;
                    state.maxZ = MAX_SIDE_BOUND2;
                } else {
                    state.minZ = MIN_SIDE_BOUND1;
                    state.maxZ = MAX_SIDE_BOUND1;
                    state.minX = MIN_SIDE_BOUND2;
                    state.maxX = MAX_SIDE_BOUND2;
                }
                state.minY = min;
                state.maxY = max;
            }
            case Z -> {
                if (rotated) {
                    state.minY = MIN_SIDE_BOUND1;
                    state.maxY = MAX_SIDE_BOUND1;
                    state.minX = MIN_SIDE_BOUND2;
                    state.maxX = MAX_SIDE_BOUND2;
                } else {
                    state.minX = MIN_SIDE_BOUND1;
                    state.maxX = MAX_SIDE_BOUND1;
                    state.minY = MIN_SIDE_BOUND2;
                    state.maxY = MAX_SIDE_BOUND2;
                }
                state.minZ = min;
                state.maxZ = max;
            }
        }
    }

    @Override
    public void submit(TeleporterRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        RenderResizableCuboid.renderCube(state.renderAxis, state.minX, state.minY, state.minZ, state.maxX, state.maxY, state.maxZ, poseStack, Sheets.translucentBlockSheet(), nodeCollector, state.tint, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), MekanismRenderer.teleporterPortal);
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.TELEPORTER;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTeleporter tile, Vec3 camera) {
        return tile.shouldRender && tile.getLevel() != null && super.shouldRender(tile, camera);
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityTeleporter tile) {
        //Note: If the frame direction is "null" we instead just only mark the teleporter itself.
        Direction frameDirection = tile.getFrameDirection();
        return frameDirection == null ? super.getRenderBoundingBox(tile) : tile.getTeleporterBoundingBox(frameDirection);
    }

    public static class TeleporterRenderState extends BlockEntityRenderState {

        public float minX, minY, minZ;
        public float maxX, maxY, maxZ;
        public int tint = CommonColors.WHITE;
        public byte renderAxis = RenderResizableCuboid.SideRender.ALL_FACES;
    }
}