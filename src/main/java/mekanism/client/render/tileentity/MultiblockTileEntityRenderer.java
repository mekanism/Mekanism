package mekanism.client.render.tileentity;

import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class MultiblockTileEntityRenderer<MULTIBLOCK extends MultiblockData, TILE extends TileEntityMultiblock<MULTIBLOCK>, STATE extends BlockEntityRenderState>
      extends MekanismTileEntityRenderer<TILE, STATE> {

    protected MultiblockTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public final boolean shouldRenderOffScreen() {
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
            VoxelCuboid bounds = multiblock.getBounds();
            if (multiblock.isFormed() && bounds != null) {
                //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
                // how the specific multiblock wants to render, that it is being viewed
                return bounds.asAABB();
            }
        }
        return super.getRenderBoundingBox(tile);
    }
}