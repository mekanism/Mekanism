package mekanism.client.render.transmitter;

import mekanism.client.render.ColourTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

public class RenderThermodynamicConductor extends RenderTransmitterSimple<TileEntityThermodynamicConductor> {

    @Override
    public void render(TileEntityThermodynamicConductor transmitter, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (MekanismConfig.current().client.opaqueTransmitters.val()) {
            return;
        }
        render(transmitter, x, y, z, 15);
    }

    @Override
    public void renderSide(BufferBuilder renderer, EnumFacing side, TileEntityThermodynamicConductor cable) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        renderTransparency(renderer, MekanismRenderer.heatIcon, getModelForSide(cable, side), ColourTemperature.fromTemperature(cable.temperature, cable.getBaseColour()));
    }
}