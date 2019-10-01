package mekanism.client.render.transmitter;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.Direction;

public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    @Override
    public void render(TileEntityUniversalCable cable, double x, double y, double z, float partialTick, int destroyStage) {
        if (!MekanismConfig.client.opaqueTransmitters.get() && cable.currentPower != 0) {
            render(cable, x, y, z, 15);
        }
    }

    @Override
    protected void renderSide(BufferBuilder renderer, Direction side, TileEntityUniversalCable cable) {
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColourRGBA(1.0, 1.0, 1.0, cable.currentPower),
              cable.getBlockState(), cable.getModelData());
    }
}