package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.MekanismRenderer.Model3D.ModelBoundsSetter;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {

    private static final Map<Direction, Model3D> modelCache = new EnumMap<>(Direction.class);
    private static final Map<Direction, Model3D> rotatedModelCache = new EnumMap<>(Direction.class);

    public static void resetCachedModels() {
        modelCache.clear();
        rotatedModelCache.clear();
    }

    public RenderTeleporter(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityTeleporter tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        MekanismRenderer.renderObject(getOverlayModel(tile.frameDirection(), tile.frameRotated()), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
              MekanismRenderer.getColorARGB(tile.getColor(), 0.75F), LightTexture.FULL_BRIGHT, overlayLight, FaceDisplay.FRONT, getCamera(), tile.getBlockPos());
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
        return cache.computeIfAbsent(direction, dir -> {
            Axis renderAxis = dir.getAxis().isHorizontal() ? Axis.Y : rotated ? Axis.X : Axis.Z;
            Model3D model = new Model3D()
                  .setTexture(MekanismRenderer.teleporterPortal)
                  .setSideRender(side -> side.getAxis() == renderAxis);
            int min = dir.getAxisDirection() == AxisDirection.POSITIVE ? 1 : -2;
            int max = dir.getAxisDirection() == AxisDirection.POSITIVE ? 3 : 0;
            return switch (dir.getAxis()) {
                case X -> {
                    setDimensions(rotated, model::zBounds, model::yBounds);
                    yield model.xBounds(min, max);
                }
                case Y -> {
                    setDimensions(rotated, model::zBounds, model::xBounds);
                    yield model.yBounds(min, max);
                }
                case Z -> {
                    setDimensions(rotated, model::xBounds, model::yBounds);
                    yield model.zBounds(min, max);
                }
            };
        });
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
    public boolean shouldRenderOffScreen(TileEntityTeleporter tile) {
        return true;
    }

    @Override
    public boolean shouldRender(TileEntityTeleporter tile, Vec3 camera) {
        return tile.shouldRender && tile.getLevel() != null && super.shouldRender(tile, camera);
    }
}