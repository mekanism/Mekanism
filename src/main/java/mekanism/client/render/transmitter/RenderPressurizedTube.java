package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.ProfilerFiller;

@ParametersAreNonnullByDefault
public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube> {

    public RenderPressurizedTube(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void render(TileEntityPressurizedTube tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight,
          ProfilerFiller profiler) {
        BoxedPressurizedTube tube = tile.getTransmitter();
        if (tube.hasTransmitterNetwork()) {
            BoxedChemicalNetwork network = tube.getTransmitterNetwork();
            if (!network.lastChemical.isEmpty() && !network.isTankEmpty() && network.currentScale > 0) {
                matrix.pushPose();
                matrix.translate(0.5, 0.5, 0.5);
                Chemical<?> chemical = network.lastChemical.getChemical();
                renderModel(tile, matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()), chemical.getTint(), Math.max(0.2F, network.currentScale),
                      MekanismRenderer.FULL_LIGHT, overlayLight, MekanismRenderer.getChemicalTexture(chemical));
                matrix.popPose();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PRESSURIZED_TUBE;
    }
}