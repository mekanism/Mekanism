package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {

    private static final Map<Direction, Model3D> modelCache = new EnumMap<>(Direction.class);
    private static final Map<Direction, Model3D> rotatedModelCache = new EnumMap<>(Direction.class);

    public static void resetCachedModels() {
        modelCache.clear();
        rotatedModelCache.clear();
    }

    public RenderTeleporter(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityTeleporter tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.shouldRender && tile.getWorld() != null) {
            MekanismRenderer.renderObject(getOverlayModel(tile.frameDirection(), tile.frameRotated()), matrix, renderer.getBuffer(Atlases.getTranslucentCullBlockType()),
                  MekanismRenderer.getColorARGB(tile.getColor(), 0.75F), MekanismRenderer.FULL_LIGHT, overlayLight);
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
            model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
            cache.put(direction, model);
            if (direction == Direction.UP) {
                model.minY = 1;
                model.maxY = 3;
                setUpDownDimensions(model, rotated);
            } else if (direction == Direction.DOWN) {
                model.minY = -2;
                model.maxY = 0;
                setUpDownDimensions(model, rotated);
            } else if (direction == Direction.EAST) {
                model.minX = 1;
                model.maxX = 3;
                setEastWestDimensions(model, rotated);
            } else if (direction == Direction.WEST) {
                model.minX = -2;
                model.maxX = 0;
                setEastWestDimensions(model, rotated);
            } else if (direction == Direction.NORTH) {
                model.minZ = -2;
                model.maxZ = 0;
                setNorthSouthDimensions(model, rotated);
            } else if (direction == Direction.SOUTH) {
                model.minZ = 0;
                model.maxZ = 3;
                setNorthSouthDimensions(model, rotated);
            }
        }
        return cache.get(direction);
    }

    private void setUpDownDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minX = 0.46;
            model.maxX = 0.54;
            model.minZ = 0;
            model.maxZ = 1;
        } else {
            model.minX = 0;
            model.maxX = 1;
            model.minZ = 0.46;
            model.maxZ = 0.54;
        }
    }

    private void setEastWestDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minY = 0.46;
            model.maxY = 0.54;
            model.minZ = 0;
            model.maxZ = 1;
        } else {
            model.minY = 0;
            model.maxY = 1;
            model.minZ = 0.46;
            model.maxZ = 0.54;
        }
    }

    private void setNorthSouthDimensions(Model3D model, boolean rotated) {
        if (rotated) {
            model.minY = 0.46;
            model.maxY = 0.54;
            model.minX = 0;
            model.maxX = 1;
        } else {
            model.minY = 0;
            model.maxY = 1;
            model.minX = 0.46;
            model.maxX = 0.54;
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTeleporter tile) {
        return tile.shouldRender && tile.getWorld() != null;
    }
}