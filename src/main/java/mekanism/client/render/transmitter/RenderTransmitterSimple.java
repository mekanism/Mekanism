package mekanism.client.render.transmitter;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;

public abstract class RenderTransmitterSimple<T extends TileEntityTransmitter<?, ?, ?>> extends RenderTransmitterBase<T> {

    protected abstract void renderSide(BufferBuilder renderer, Direction side, T transmitter);

    protected void render(@Nonnull T transmitter, double x, double y, double z, int glow) {
        RenderSystem.pushMatrix();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.disableLighting();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        field_228858_b_.textureManager.bindTexture(PlayerContainer.field_226615_c_);
        RenderSystem.translatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);

        for (Direction side : EnumUtils.DIRECTIONS) {
            renderSide(worldRenderer, side, transmitter);
        }

        GlowInfo glowInfo = MekanismRenderer.enableGlow(glow);
        tessellator.draw();
        MekanismRenderer.disableGlow(glowInfo);
        RenderSystem.enableLighting();
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.popMatrix();
    }
}