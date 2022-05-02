package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
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
        if (tile.shouldRender && tile.getLevel() != null) {
            MekanismRenderer.renderObject(getOverlayModel(tile.frameDirection(), tile.frameRotated()), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                  MekanismRenderer.getColorARGB(tile.getColor(), 0.75F), MekanismRenderer.FULL_LIGHT, overlayLight, FaceDisplay.FRONT);
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
        if (!cache.containsKey(direction)) {
            Model3D model = new Model3D();
            model.setTexture(MekanismRenderer.teleporterPortal);
            cache.put(direction, model);
            switch (direction) {
                case UP -> {
                    model.minY = 1;
                    model.maxY = 3;
                    setUpDownDimensions(model, rotated);
                }
                case DOWN -> {
                    model.minY = -2;
                    model.maxY = 0;
                    setUpDownDimensions(model, rotated);
                }
                case EAST -> {
                    model.minX = 1;
                    model.maxX = 3;
                    setEastWestDimensions(model, rotated);
                }
                case WEST -> {
                    model.minX = -2;
                    model.maxX = 0;
                    setEastWestDimensions(model, rotated);
                }
                case NORTH -> {
                    model.minZ = -2;
                    model.maxZ = 0;
                    setNorthSouthDimensions(model, rotated);
                }
                case SOUTH -> {
                    model.minZ = 0;
                    model.maxZ = 3;
                    setNorthSouthDimensions(model, rotated);
                }
            }
        }
        return cache.get(direction);
    }

    private void setUpDownDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minX = 0.46F;
            model.maxX = 0.54F;
            model.minZ = 0;
            model.maxZ = 1;
        } else {
            model.minX = 0;
            model.maxX = 1;
            model.minZ = 0.46F;
            model.maxZ = 0.54F;
        }
    }

    private void setEastWestDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minY = 0.46F;
            model.maxY = 0.54F;
            model.minZ = 0;
            model.maxZ = 1;
        } else {
            model.minY = 0;
            model.maxY = 1;
            model.minZ = 0.46F;
            model.maxZ = 0.54F;
        }
    }

    private void setNorthSouthDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minY = 0.46F;
            model.maxY = 0.54F;
            model.minX = 0;
            model.maxX = 1;
        } else {
            model.minY = 0;
            model.maxY = 1;
            model.minX = 0.46F;
            model.maxX = 0.54F;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityTeleporter tile) {
        return tile.shouldRender && tile.getLevel() != null;
    }
}