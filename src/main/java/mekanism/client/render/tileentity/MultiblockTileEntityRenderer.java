package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@NothingNullByDefault
public abstract class MultiblockTileEntityRenderer<MULTIBLOCK extends MultiblockData, TILE extends TileEntityMultiblock<MULTIBLOCK>> extends MekanismTileEntityRenderer<TILE> {

    protected MultiblockTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected final void render(TILE tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler) {
        render(tile, tile.getMultiblock(), partialTick, matrix, renderer, light, overlayLight, profiler);
    }

    protected abstract void render(TILE tile, MULTIBLOCK multiblock, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler);

    @Override
    public final boolean shouldRenderOffScreen(TILE tile) {
        return true;
    }

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
            if (multiblock.isFormed() && multiblock.getBounds() != null) {
                //TODO: Eventually we may want to look into caching this
                //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
                // how the specific multiblock wants to render, that it is being viewed
                return new AABB(multiblock.getMinPos(), multiblock.getMaxPos().offset(1, 1, 1));
            }
        }
        return super.getRenderBoundingBox(tile);
    }
}