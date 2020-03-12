package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    public RenderUniversalCable(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityUniversalCable cable, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (!MekanismConfig.client.opaqueTransmitters.get() && cable.currentPower > 0) {
            render(cable, matrix, renderer, light, overlayLight, 15, profiler);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.UNIVERSAL_CABLE;
    }

    @Override
    protected void renderContents(MatrixStack matrix, IVertexBuilder renderer, TileEntityUniversalCable cable, int light, int overlayLight) {
        renderModel(cable, matrix, renderer, 1, 1, 1, (float) cable.currentPower, light, overlayLight, MekanismRenderer.energyIcon);
    }
}