package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.transmitter.ThermodynamicConductor;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderThermodynamicConductor extends RenderTransmitterBase<TileEntityThermodynamicConductor> {

    public RenderThermodynamicConductor(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityThermodynamicConductor tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        ThermodynamicConductor conductor = tile.getTransmitter();
        int argb = HeatUtils.getColorFromTemp(conductor.getTotalTemperature(), conductor.getBaseColor()).argb();
        renderModel(tile, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), argb, MekanismRenderer.getAlpha(argb), MekanismRenderer.FULL_LIGHT,
              overlayLight, MekanismRenderer.heatIcon);
        matrix.popPose();
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.THERMODYNAMIC_CONDUCTOR;
    }
}