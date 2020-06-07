package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.content.transmitter.Transmitter;
import mekanism.common.content.transmitter.grid.EnergyNetwork;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.profiler.IProfiler;

@ParametersAreNonnullByDefault
public class RenderUniversalCable extends RenderTransmitterBase<TileEntityUniversalCable> {

    public RenderUniversalCable(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    protected void render(TileEntityUniversalCable cable, float partialTick, MatrixStack matrix, IRenderTypeBuffer renderer, int light, int overlayLight,
          IProfiler profiler) {
        Transmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter = cable.getTransmitter();
        if (transmitter.hasTransmitterNetwork()) {
            EnergyNetwork network = transmitter.getTransmitterNetwork();
            //Note: We don't check if the network is empty as we don't actually ever sync the energy value to the client
            if (network.energyScale > 0) {
                matrix.push();
                matrix.translate(0.5, 0.5, 0.5);
                renderModel(cable, matrix, renderer.getBuffer(MekanismRenderType.transmitterContents(AtlasTexture.LOCATION_BLOCKS_TEXTURE)), 0xFFFFFF,
                      network.energyScale, MekanismRenderer.FULL_LIGHT, overlayLight, MekanismRenderer.energyIcon);
                matrix.pop();
            }
        }
    }

    @Override
    protected String getProfilerSection() {
        return ProfilerConstants.UNIVERSAL_CABLE;
    }
}