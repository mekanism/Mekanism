package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.transmitter.BoxedPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube> {

    public RenderPressurizedTube(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityPressurizedTube tile, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        BoxedPressurizedTube tube = tile.getTransmitter();
        if (tube.hasTransmitterNetwork()) {
            BoxedChemicalNetwork network = tube.getTransmitterNetwork();
            if (!network.lastChemical.isEmpty() && !network.isTankEmpty() && network.currentScale > 0) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                Chemical<?> chemical = network.lastChemical.getChemical();
                renderModel(tile, matrix, renderer.getBuffer(Atlases.getTranslucentCullBlockType()), chemical.getTint(), Math.max(0.2F, network.currentScale),
                      MekanismRenderer.FULL_LIGHT, overlayLight, MekanismRenderer.getChemicalTexture(chemical));
                matrix.pop();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PRESSURIZED_TUBE;
    }
}