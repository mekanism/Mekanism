package mekanism.client.render.transmitter;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;

public abstract class RenderTransmitterSimple<T extends TileEntityTransmitter<?, ?, ?>> extends RenderTransmitterBase<T> {

    public RenderTransmitterSimple(TileEntityRendererDispatcher renderer) {
        super(renderer);
    }

    protected abstract void renderContents(MatrixStack matrix, IVertexBuilder renderer, T transmitter, int light, int overlayLight);

    //TODO: 1.15, replace places that name it otherLight with overlayLight as it seems like a better name
    protected void render(@Nonnull T transmitter, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight, int glow) {
        matrix.func_227860_a_();
        //RenderSystem.enableCull();
        //RenderSystem.enableBlend();
        //RenderSystem.disableLighting();
        //RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        //TODO: 1.15 Figure out which render type to use
        IVertexBuilder buffer = renderer.getBuffer(RenderType.func_228642_d_(PlayerContainer.field_226615_c_));
        matrix.func_227861_a_(0.5, 0.5, 0.5);
        //TODO: 1.15 Figure out glow
        //GlowInfo glowInfo = MekanismRenderer.enableGlow(glow);
        renderContents(matrix, buffer, transmitter, light, otherLight);
        //MekanismRenderer.disableGlow(glowInfo);
        //RenderSystem.enableLighting();
        //RenderSystem.disableBlend();
        //RenderSystem.disableCull();
        matrix.func_227865_b_();
    }
}