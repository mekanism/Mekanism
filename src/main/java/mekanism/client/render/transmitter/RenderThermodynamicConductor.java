package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.ColourTemperature;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityThermodynamicConductor;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderThermodynamicConductor extends RenderTransmitterSimple<TileEntityThermodynamicConductor> {

    public RenderThermodynamicConductor(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityThermodynamicConductor transmitter, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer,
          int light, int otherLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get()) {
            render(transmitter, matrix, renderer, light, otherLight, 15);
        }
    }

    @Override
    public void renderContents(MatrixStack matrix, IVertexBuilder renderer, @Nonnull TileEntityThermodynamicConductor conductor, int light, int overlayLight) {
        //TODO: Fix the fact that this seems to always be whitish when it would make more sense to be orangeish
        renderModel(conductor, matrix, renderer, light, overlayLight, MekanismRenderer.heatIcon, ColourTemperature.fromTemperature(conductor.getTemp(), conductor.getBaseColour()));
    }
}