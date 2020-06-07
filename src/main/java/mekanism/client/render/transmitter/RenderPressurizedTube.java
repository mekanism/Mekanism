package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.transmitter.GasNetwork;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube> {

    public RenderPressurizedTube(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityPressurizedTube tube, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        if (tube.hasTransmitterNetwork()) {
            GasNetwork network = tube.getTransmitterNetwork();
            if (!network.lastGas.isEmptyType() && !network.gasTank.isEmpty() && network.gasScale > 0) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                renderModel(tube, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)), network.lastGas.getTint(),
                      network.gasScale, MekanismRenderer.FULL_LIGHT, overlayLight, MekanismRenderer.getChemicalTexture(network.lastGas));
                matrix.pop();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PRESSURIZED_TUBE;
    }
}