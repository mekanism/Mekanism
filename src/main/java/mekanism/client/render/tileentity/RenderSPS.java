package mekanism.client.render.tileentity;

import javax.annotation.ParametersAreNonnullByDefault;
import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderSPS extends MekanismTileEntityRenderer<TileEntitySPSCasing> {

    public RenderSPS(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntitySPSCasing tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight, IProfiler profiler) {
        if (tile.isMaster && tile.getMultiblock().isFormed() && tile.getMultiblock().renderLocation != null) {

        }
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
