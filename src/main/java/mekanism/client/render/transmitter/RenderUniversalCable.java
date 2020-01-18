package mekanism.client.render.transmitter;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColorRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;

public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    @Override
    public void render(TileEntityUniversalCable cable, double x, double y, double z, float partialTick, int destroyStage) {
        if (!MekanismConfig.client.opaqueTransmitters.get() && cable.currentPower > 0) {
            render(cable, x, y, z, 15);
        }
    }

    @Override
    protected void renderSide(BufferBuilder renderer, Direction side, @Nonnull TileEntityUniversalCable cable) {
        renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColorRGBA(1.0, 1.0, 1.0, cable.currentPower),
              cable.getBlockState(), cable.getModelData());
    }
}