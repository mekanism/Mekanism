package mekanism.client.render.transmitter;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;

public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    @Override
    public void render(TileEntityUniversalCable cable, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (MekanismConfig.current().client.opaqueTransmitters.val() || cable.currentPower == 0) {
            return;
        }
        render(cable, x, y, z, 15);
    }

    @Override
    protected void renderSide(BufferBuilder renderer, EnumFacing side, TileEntityUniversalCable cable) {
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColourRGBA(1.0, 1.0, 1.0, cable.currentPower));
    }
}