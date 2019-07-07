package mekanism.client.render.transmitter;

import mekanism.client.render.GLSMHelper;
import mekanism.client.render.GLSMHelper.GlowInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumFacing;

public abstract class RenderTransmitterSimple<T extends TileEntityTransmitter> extends RenderTransmitterBase<T> {

    protected abstract void renderSide(BufferBuilder renderer, EnumFacing side, T transmitter);

    protected void render(T transmitter, double x, double y, double z, int glow) {
        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        GlStateManager.translate((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

        for (EnumFacing side : EnumFacing.VALUES) {
            renderSide(worldRenderer, side, transmitter);
        }

        GlowInfo glowInfo = GLSMHelper.enableGlow(glow);
        tessellator.draw();
        GLSMHelper.disableGlow(glowInfo);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }
}