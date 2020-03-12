package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.ColorTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderThermodynamicConductor extends RenderTransmitterSimple<TileEntityThermodynamicConductor> {

    public RenderThermodynamicConductor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityThermodynamicConductor transmitter, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            render(transmitter, matrix, renderer, light, overlayLight, 15, profiler);
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMODYNAMIC_CONDUCTOR;
    }

    @Override
    public void renderContents(MatrixStack matrix, IVertexBuilder renderer, TileEntityThermodynamicConductor conductor, int light, int overlayLight) {
        renderModel(conductor, matrix, renderer, light, overlayLight, MekanismRenderer.heatIcon, ColorTemperature.fromTemperature(conductor.getTemp(), conductor.getBaseColor()));
    }
}