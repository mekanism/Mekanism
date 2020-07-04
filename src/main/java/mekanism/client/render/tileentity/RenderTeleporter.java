package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.TileEntityTeleporter;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Direction;

@ParametersAreNonnullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {

    private static Map<Direction, Model3D> modelCache = new HashMap<>();
    private static Map<Direction, Model3D> rotatedModelCache = new HashMap<>();

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
            MekanismRenderer.renderObject(getOverlayModel(tile.frameDirection(), tile.frameRotated()), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(EnumColor.PURPLE, 0.75F), MekanismRenderer.FULL_LIGHT);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.TELEPORTER;
    }

    private Model3D getOverlayModel(Direction direction, boolean rotated) {
    	if(direction == null) {
    		direction = Direction.UP;
    	}
        Map<Direction, Model3D> cache = rotated ? rotatedModelCache : modelCache;
        if (!cache.containsKey(direction)) {
            switch (direction) {
            case UP:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 1;
                    model.maxY = 3;
                    model.minX = 0.46;
                    model.maxX = 0.54;
                    model.minZ = 0;
                    model.maxZ = 1;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 1;
                    model.maxY = 3;
                    model.minX = 0;
                    model.maxX = 1;
                    model.minZ = 0.46;
                    model.maxZ = 0.54;
                    cache.put(direction, model);
                }
            break;

            case DOWN:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = -2;
                    model.maxY = 0;
                    model.minX = 0.46;
                    model.maxX = 0.54;
                    model.minZ = 0;
                    model.maxZ = 1;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = -2;
                    model.maxY = 0;
                    model.minX = 0;
                    model.maxX = 1;
                    model.minZ = 0.46;
                    model.maxZ = 0.54;
                    cache.put(direction, model);
                }
                break;

            case EAST:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0.46;
                    model.maxY = 0.54;
                    model.minX = 1;
                    model.maxX = 3;
                    model.minZ = 0;
                    model.maxZ = 1;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0;
                    model.maxY = 1;
                    model.minX = 1;
                    model.maxX = 3;
                    model.minZ = 0.46;
                    model.maxZ = 0.54;
                    cache.put(direction, model);
                }
                break;

            case WEST:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0.46;
                    model.maxY = 0.54;
                    model.minX = -2;
                    model.maxX = 0;
                    model.minZ = 0;
                    model.maxZ = 1;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0;
                    model.maxY = 1;
                    model.minX = -2;
                    model.maxX = 0;
                    model.minZ = 0.46;
                    model.maxZ = 0.54;
                    cache.put(direction, model);
                }
                break;

            case NORTH:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0.46;
                    model.maxY = 0.54;
                    model.minX = 0;
                    model.maxX = 1;
                    model.minZ = -2;
                    model.maxZ = 0;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0;
                    model.maxY = 1;
                    model.minX = 0.46;
                    model.maxX = 0.54;
                    model.minZ = -2;
                    model.maxZ = 0;
                    cache.put(direction, model);
                }
                break;

            case SOUTH:
                if (rotated) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0.46;
                    model.maxY = 0.54;
                    model.minX = 0;
                    model.maxX = 1;
                    model.minZ = 0;
                    model.maxZ = 3;
                    cache.put(direction, model);
                } else {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 0;
                    model.maxY = 1;
                    model.minX = 0.46;
                    model.maxX = 0.54;
                    model.minZ = 0;
                    model.maxZ = 3;
                    cache.put(direction, model);
                }
                break;

            default:
                if (!cache.containsKey(Direction.UP)) {
                    Model3D model = new Model3D();
                    model.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                    model.minY = 1;
                    model.maxY = 3;
                    model.minX = 0;
                    model.maxX = 1;
                    model.minZ = 0.46;
                    model.maxZ = 0.54;
                    cache.put(Direction.UP, model);
                }
                return cache.get(Direction.UP);
            }
        }
        return cache.get(direction);
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTeleporter tile) {
        return tile.shouldRender && tile.getWorld() != null;
    }
}