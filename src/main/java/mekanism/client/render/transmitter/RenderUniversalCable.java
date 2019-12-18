package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Direction;

public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    @Override
    public void func_225616_a_(@Nonnull TileEntityUniversalCable cable, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get() && cable.currentPower > 0) {
            //TODO: 1.15
            //render(cable, x, y, z, 15);
        }
    }

    @Override
    protected void renderSide(BufferBuilder renderer, Direction side, @Nonnull TileEntityUniversalCable cable) {
        //TODO: 1.15
        /*renderTransparency(renderer, MekanismRenderer.energyIcon, getModelForSide(cable, side), new ColourRGBA(1.0, 1.0, 1.0, cable.currentPower),
              cable.getBlockState(), cable.getModelData());*/
    }
}