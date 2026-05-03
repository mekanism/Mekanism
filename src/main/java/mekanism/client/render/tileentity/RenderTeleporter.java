package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.ModelBoundsSetter;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.client.render.tileentity.RenderTeleporter.TeleporterRenderState;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter, TeleporterRenderState> {

    private static final Map<Direction, Model3D> modelCache = new EnumMap<>(Direction.class);
    private static final Map<Direction, Model3D> rotatedModelCache = new EnumMap<>(Direction.class);
    private static final Function<Direction, TextureAtlasSprite> PORTAL_TEXUTURE = _ -> MekanismRenderer.teleporterPortal;

    public static void resetCachedModels() {
        modelCache.clear();
        rotatedModelCache.clear();
    }

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
        state.model = getOverlayModel(teleporter.frameDirection(), teleporter.frameRotated());
    }

    @Override
    public void submit(TeleporterRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        if (state.model != null) {
            RenderResizableCuboid.renderCube(state.model, poseStack, Sheets.translucentBlockSheet(), nodeCollector, state.tint, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, camera.pos, Vec3.atLowerCornerOf(state.blockPos), PORTAL_TEXUTURE);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.TELEPORTER;
    }

    private Model3D getOverlayModel(@Nullable Direction direction, boolean rotated) {
        if (direction == null) {
            direction = Direction.UP;
        }
        Map<Direction, Model3D> cache = rotated ? rotatedModelCache : modelCache;
        Model3D model = cache.get(direction);
        if (model == null) {
            model = new Model3D();
            Axis renderAxis = direction.getAxis().isHorizontal() ? Axis.Y : rotated ? Axis.X : Axis.Z;
            for (Direction side : EnumUtils.DIRECTIONS) {
                model.setSideRender(direction, side.getAxis() == renderAxis);
            }
            int min = direction.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -2;
            int max = direction.getAxisDirection() == AxisDirection.POSITIVE ? 3 : 0;
            switch (direction.getAxis()) {
                case X -> {
                    setDimensions(rotated, model::zBounds, model::yBounds);
                    model.xBounds(min, max);
                }
                case Y -> {
                    setDimensions(rotated, model::zBounds, model::xBounds);
                    model.yBounds(min, max);
                }
                case Z -> {
                    setDimensions(rotated, model::xBounds, model::yBounds);
                    model.zBounds(min, max);
                }
            }
            cache.put(direction, model);
        }
        return model;
    }

    private void setDimensions(boolean rotated, ModelBoundsSetter setter1, ModelBoundsSetter setter2) {
        if (rotated) {
            setDimensions(false, setter2, setter1);
        } else {
            setter1.set(0.46F, 0.54F);
            setter2.set(0, 1);
        }
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

        @Nullable
        public Model3D model;
        public int tint = 0xFFFFFFFF;
    }
}