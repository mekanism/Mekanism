package mekanism.client.render.transmitter;

import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.transmitters.TransmitterImpl;
import mekanism.common.transmitters.grid.GasNetwork;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.Direction;

public class RenderPressurizedTube extends RenderTransmitterSimple<TileEntityPressurizedTube> {

    @Override
    public void render(TileEntityPressurizedTube tube, double x, double y, double z, float partialTick, int destroyStage) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            TransmitterImpl<IGasHandler, GasNetwork, GasStack> transmitter = tube.getTransmitter();
            if (transmitter.hasTransmitterNetwork()) {
                GasNetwork transmitterNetwork = transmitter.getTransmitterNetwork();
                if (!transmitterNetwork.buffer.isEmpty() && transmitterNetwork.gasScale != 0) {
                    render(tube, x, y, z, 0);
                }
            }
        }
    }

    @Override
    protected void renderSide(BufferBuilder renderer, Direction side, @Nonnull TileEntityPressurizedTube tube) {
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        GasStack gasStack = tube.getTransmitter().getTransmitterNetwork().buffer;
        if (!gasStack.isEmpty()) {
            //Double check it is not empty
            Gas gas = gasStack.getType();
            ColourRGBA c = new ColourRGBA(1.0, 1.0, 1.0, tube.currentScale);
            c.setRGBFromInt(gas.getTint());
            renderTransparency(renderer, gas.getSprite(), getModelForSide(tube, side), c, tube.getBlockState(), tube.getModelData());
        }
    }
}