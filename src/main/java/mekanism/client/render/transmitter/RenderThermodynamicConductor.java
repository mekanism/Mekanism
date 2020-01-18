package mekanism.client.render.transmitter;

import javax.annotation.Nonnull;

import mekanism.client.render.ColorTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;

public class RenderThermodynamicConductor extends RenderTransmitterSimple<TileEntityThermodynamicConductor> {

    @Override
    public void render(TileEntityThermodynamicConductor transmitter, double x, double y, double z, float partialTick, int destroyStage) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            render(transmitter, x, y, z, 15);
        }
    }

    @Override
    public void renderSide(BufferBuilder renderer, Direction side, @Nonnull TileEntityThermodynamicConductor cable) {
        //TODO: Fix the fact that this seems to always be whitish
        renderTransparency(renderer, MekanismRenderer.heatIcon, getModelForSide(cable, side), ColorTemperature.fromTemperature(cable.getTemp(), cable.getBaseColor()),
              cable.getBlockState(), cable.getModelData());
    }
}