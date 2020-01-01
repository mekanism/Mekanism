package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class RenderUniversalCable extends RenderTransmitterSimple<TileEntityUniversalCable> {

    public RenderUniversalCable(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    @Override
    public void func_225616_a_(@Nonnull TileEntityUniversalCable cable, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light,
          int otherLight) {
        if (!MekanismConfig.client.opaqueTransmitters.get() && cable.currentPower > 0) {
            render(cable, matrix, renderer, light, otherLight, 15);
        }
    }

    @Override
    protected void renderContents(MatrixStack matrix, IVertexBuilder renderer, @Nonnull TileEntityUniversalCable cable, int light, int overlayLight) {
        renderModel(cable, matrix, renderer, 1, 1, 1, (float) cable.currentPower, light, overlayLight, MekanismRenderer.energyIcon);
    }
}