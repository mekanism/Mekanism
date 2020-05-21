package mekanism.client.render.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.render.bolt.BoltEffect;
import mekanism.client.render.bolt.BoltRenderer;
import mekanism.client.render.bolt.BoltRenderer.BoltData;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData.CoilData;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.Vec3d;

@ParametersAreNonnullByDefault
public class RenderSPS extends MekanismTileEntityRenderer<TileEntitySPSCasing> {

    private BoltRenderer bolts = BoltRenderer.create(BoltEffect.ELECTRICITY);

    public RenderSPS(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySPSCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null) {
            for (CoilData data : tile.getMultiblock().coilData.coilMap.values()) {
                if (data.prevLevel > 0) {
                    bolts.update(data.coilPos.hashCode(), getBoltFromData(data, tile.getMultiblock()), partialTick);
                }
            }

            if (tile.getMultiblock().lastProcessed > 0) {
                // render core
            }

            bolts.render(partialTick, matrix, renderer);
        }
    }

    private static BoltData getBoltFromData(CoilData data, SPSMultiblockData multiblock) {
        Vec3d start = new Vec3d(data.coilPos).add(0.5, 0.5, 0.5);
        start = start.add(new Vec3d(data.side.getDirectionVec()).scale(0.5));
        // center of multiblock
        Vec3d end = new Vec3d(multiblock.minLocation).add(new Vec3d(multiblock.maxLocation)).add(new Vec3d(1, 1, 1)).scale(0.5);
        int count = 1 + (data.prevLevel - 1) / 2;
        float size = 0.01F * data.prevLevel;
        return new BoltData(start, end, count, 5, size);
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.SPS;
    }

    @Override
    public boolean isGlobalRenderer(TileEntitySPSCasing tile) {
        return tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null;
    }
}
