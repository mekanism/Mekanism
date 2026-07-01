package mekanism.client.render.tileentity;

import java.util.Objects;
import mekanism.client.render.MultiblockContentsRenderState;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class MultiblockTileEntityRenderer<MULTIBLOCK extends MultiblockData, TILE extends TileEntityMultiblock<MULTIBLOCK>, STATE extends MultiblockContentsRenderState>
      extends MekanismTileEntityRenderer<TILE, STATE> {

    protected MultiblockTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public final boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public final void extractRenderState(TILE tile, STATE state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tile, state, partialTick, cameraPosition, breakProgress);
        MULTIBLOCK multiblock = tile.getMultiblock();
        state.length = multiblock.length() - 2;
        state.width = multiblock.width() - 2;
        state.height = multiblock.height() - 2;
        //Sanity check that the bounds are valid
        if (state.length > 0 && state.width > 0 && state.height > 0) {
            state.renderLocation = Objects.requireNonNull(multiblock.renderLocation, "Render location may not be null.").offset(1, 0, 1);
            extractRenderState(tile, multiblock, state, partialTick, cameraPosition, breakProgress);
        }
    }

    public abstract void extractRenderState(TILE tile, MULTIBLOCK multiblock, STATE state, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress);

    @Override
    public final boolean shouldRender(TILE tile, Vec3 camera) {
        if (tile.isMaster()) {
            MULTIBLOCK multiblock = tile.getMultiblock();
            return multiblock.isFormed() && shouldRender(tile, multiblock, camera) && super.shouldRender(tile, camera);
        }
        return false;
    }

    protected boolean shouldRender(TILE tile, MULTIBLOCK multiblock, Vec3 camera) {
        return multiblock.renderLocation != null;
    }

    @Override
    public AABB getRenderBoundingBox(TILE tile) {
        if (tile.isMaster()) {
            MULTIBLOCK multiblock = tile.getMultiblock();
            if (multiblock.isFormed()) {
                //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
                // how the specific multiblock wants to render, that it is being viewed
                return multiblock.getBounds().asAABB();
            }
        }
        return super.getRenderBoundingBox(tile);
    }
}