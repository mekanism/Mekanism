package mekanism.client.render.transmitter;

import mekanism.api.gas.Gas;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class RenderPressurizedTube extends RenderTransmitterBase<TileEntityPressurizedTube> {

    @Override
    public void render(TileEntityPressurizedTube tube, double x, double y, double z, float partialTick, int destroyStage, float alpha) {
        if (MekanismConfig.current().client.opaqueTransmitters.val() || !tube.getTransmitter().hasTransmitterNetwork()
            || tube.getTransmitter().getTransmitterNetwork().refGas == null || tube.getTransmitter().getTransmitterNetwork().gasScale == 0) {
            return;
        }

        push();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

        for (EnumFacing side : EnumFacing.VALUES) {
            renderGasSide(worldRenderer, side, tube);
        }

        MekanismRenderer.glowOn(0);

        tessellator.draw();

        MekanismRenderer.glowOff();

        pop();
    }

    public void renderGasSide(BufferBuilder renderer, EnumFacing side, TileEntityPressurizedTube tube) {
        bindTexture(MekanismRenderer.getBlocksTexture());
        Gas gas = tube.getTransmitter().getTransmitterNetwork().refGas;
        ColourRGBA c = new ColourRGBA(1.0, 1.0, 1.0, tube.currentScale);
        c.setRGBFromInt(gas.getTint());
        renderTransparency(renderer, gas.getSprite(), getModelForSide(tube, side), c);
    }
}