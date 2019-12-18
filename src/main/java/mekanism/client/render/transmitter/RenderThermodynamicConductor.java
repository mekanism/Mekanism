package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Direction;

public class RenderThermodynamicConductor extends RenderTransmitterSimple<TileEntityThermodynamicConductor> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityThermodynamicConductor transmitter, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int otherLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            //TODO: 1.15
            //render(transmitter, x, y, z, 15);
        }
    }

    @Override
    public void renderSide(BufferBuilder renderer, Direction side, @Nonnull TileEntityThermodynamicConductor cable) {
        //TODO: Fix the fact that this seems to always be whitish
        //TODO: 1.15
        /*renderTransparency(renderer, MekanismRenderer.heatIcon, getModelForSide(cable, side), ColourTemperature.fromTemperature(cable.getTemp(), cable.getBaseColour()),
              cable.getBlockState(), cable.getModelData());*/
    }
}