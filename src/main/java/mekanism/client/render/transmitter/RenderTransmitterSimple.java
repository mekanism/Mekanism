package mekanism.client.render.transmitter;

import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;

public abstract class RenderTransmitterSimple<T extends TileEntityTransmitter> extends RenderTransmitterBase<T> {

    protected abstract void renderSide(BufferBuilder renderer, EnumFacing side, T transmitter);

    protected void render(T transmitter, double x, double y, double z, int glow) {
        MekanismRenderHelper renderHelper = initHelper();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        for (EnumFacing side : EnumFacing.VALUES) {
            renderSide(worldRenderer, side, transmitter);
        }

        MekanismRenderer.glowOn(glow);
        tessellator.draw();
        MekanismRenderer.glowOff();
        renderHelper.cleanup();
    }
}