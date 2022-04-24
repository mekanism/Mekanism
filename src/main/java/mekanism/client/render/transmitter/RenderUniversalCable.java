package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderUniversalCable extends RenderTransmitterBase<TileEntityUniversalCable> {

    public RenderUniversalCable(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityUniversalCable tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        UniversalCable cable = tile.getTransmitter();
        if (cable.hasTransmitterNetwork()) {
            EnergyNetwork network = cable.getTransmitterNetwork();
            //Note: We don't check if the network is empty as we don't actually ever sync the energy value to the client
            if (network.currentScale > 0) {
                matrix.pushPose();
                matrix.translate(0.5, 0.5, 0.5);
                renderModel(tile, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), 0xFFFFFF, network.currentScale, MekanismRenderer.FULL_LIGHT,
                      overlayLight, MekanismRenderer.energyIcon);
                matrix.popPose();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.UNIVERSAL_CABLE;
    }
}