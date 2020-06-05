package mekanism.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
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

@ParametersAreNonnullByDefault
public class RenderTeleporter extends MekanismTileEntityRenderer<TileEntityTeleporter> {

    private static Model3D EAST_WEST;
    private static Model3D NORTH_SOUTH;

    public static void resetCachedModels() {
        EAST_WEST = null;
        NORTH_SOUTH = null;
    }

    public RenderTeleporter(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityTeleporter tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.shouldRender && tile.getWorld() != null) {
            MekanismRenderer.renderObject(getOverlayModel(tile.hasEastWestFrame()), matrix, renderer.getBuffer(MekanismRenderType.resizableCuboid()),
                  MekanismRenderer.getColorARGB(EnumColor.PURPLE, 0.75F), MekanismRenderer.FULL_LIGHT);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.TELEPORTER;
    }

    private Model3D getOverlayModel(boolean eastWest) {
        if (eastWest) {
            if (EAST_WEST == null) {
                EAST_WEST = new Model3D();
                EAST_WEST.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
                EAST_WEST.minY = 1;
                EAST_WEST.maxY = 3;
                EAST_WEST.minX = 0;
                EAST_WEST.minZ = 0.46;
                EAST_WEST.maxX = 1;
                EAST_WEST.maxZ = 0.54;
            }
            return EAST_WEST;
        }
        if (NORTH_SOUTH == null) {
            NORTH_SOUTH = new Model3D();
            NORTH_SOUTH.setTexture(MekanismRenderer.getChemicalTexture(MekanismGases.HYDROGEN.getChemical()));
            NORTH_SOUTH.minY = 1;
            NORTH_SOUTH.maxY = 3;
            NORTH_SOUTH.minX = 0.46;
            NORTH_SOUTH.minZ = 0;
            NORTH_SOUTH.maxX = 0.54;
            NORTH_SOUTH.maxZ = 1;
        }
        return NORTH_SOUTH;
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTeleporter tile) {
        return tile.shouldRender && tile.getWorld() != null;
    }
}