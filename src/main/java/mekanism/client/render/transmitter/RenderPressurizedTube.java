package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.GasNetwork;
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
        TransmitterImpl<IGasHandler, GasNetwork, GasStack> transmitter = tube.getTransmitter();
        if (transmitter.hasTransmitterNetwork()) {
            GasNetwork transmitterNetwork = transmitter.getTransmitterNetwork();
            if (!transmitterNetwork.gasTank.isEmpty() && transmitterNetwork.gasScale > 0) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                Gas gas = transmitterNetwork.gasTank.getType();
                renderModel(tube, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)), gas.getTint(),
                      tube.currentScale, MekanismRenderer.FULL_LIGHT, overlayLight, MekanismRenderer.getChemicalTexture(gas));
                matrix.pop();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.PRESSURIZED_TUBE;
    }
}