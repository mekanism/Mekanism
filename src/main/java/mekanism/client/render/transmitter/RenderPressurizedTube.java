package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.GasNetwork;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderPressurizedTube extends RenderTransmitterSimple<TileEntityPressurizedTube> {

    public RenderPressurizedTube(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityPressurizedTube tube, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int overlayLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            TransmitterImpl<IGasHandler, GasNetwork, GasStack> transmitter = tube.getTransmitter();
            if (transmitter.hasTransmitterNetwork()) {
                GasNetwork transmitterNetwork = transmitter.getTransmitterNetwork();
                if (!transmitterNetwork.buffer.isEmpty() && transmitterNetwork.gasScale > 0) {
                    render(tube, matrix, renderer, light, overlayLight, 0);
                }
            }
        }
    }

    @Override
    protected void renderContents(MatrixStack matrix, IVertexBuilder renderer, @Nonnull TileEntityPressurizedTube tube, int light, int overlayLight) {
        Gas gas = tube.getTransmitter().getTransmitterNetwork().buffer.getType();
        int tint = gas.getTint();
        renderModel(tube, matrix, renderer, MekanismRenderer.getRed(tint), MekanismRenderer.getGreen(tint), MekanismRenderer.getBlue(tint), tube.currentScale, light,
              overlayLight, MekanismRenderer.getChemicalTexture(gas));
    }
}