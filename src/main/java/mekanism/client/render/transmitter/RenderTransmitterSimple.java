package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;

public abstract class RenderTransmitterSimple<T extends TileEntityTransmitter<?, ?, ?>> extends RenderTransmitterBase<T> {

    public RenderTransmitterSimple(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    protected abstract void renderContents(MatrixStack matrix, IVertexBuilder renderer, T transmitter, int light, int overlayLight);

    protected void render(@Nonnull T transmitter, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, int glow) {
        matrix.func_227860_a_();
        IVertexBuilder buffer = renderer.getBuffer(MekanismRenderType.transmitterContents(PlayerContainer.field_226615_c_));
        matrix.func_227861_a_(0.5, 0.5, 0.5);
        GlowInfo glowInfo = MekanismRenderer.enableGlow(glow);
        renderContents(matrix, buffer, transmitter, light, overlayLight);
        MekanismRenderer.disableGlow(glowInfo);
        matrix.func_227865_b_();
    }
}