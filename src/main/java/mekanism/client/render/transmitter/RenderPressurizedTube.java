package mekanism.client.render.transmitter;

import mekanism.api.gas.Gas;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

public class RenderPressurizedTube extends RenderTransmitterSimple<TileEntityPressurizedTube> {

    @Override
    public void render(TileEntityPressurizedTube tube, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (MekanismConfig.current().client.opaqueTransmitters.val() || !tube.getTransmitter().hasTransmitterNetwork()
            || tube.getTransmitter().getTransmitterNetwork().refGas == null || tube.getTransmitter().getTransmitterNetwork().gasScale == 0) {
            return;
        }
        render(tube, x, y, z, 0);
    }

    @Override
    protected void renderSide(BufferBuilder renderer, EnumFacing side, TileEntityPressurizedTube tube) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Gas gas = tube.getTransmitter().getTransmitterNetwork().refGas;
        ColourRGBA c = new ColourRGBA(1.0, 1.0, 1.0, tube.currentScale);
        c.setRGBFromInt(gas.getTint());
        renderTransparency(renderer, gas.getSprite(), getModelForSide(tube, side), c);
    }
}