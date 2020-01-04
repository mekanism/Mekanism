package mekanism.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class RenderFlame extends EntityRenderer<EntityFlame> {

    public RenderFlame(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void func_225623_a_(@Nonnull EntityFlame flame, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        if (flame.ticksExisted < 1) {
            return;
        }
        float alpha = (flame.ticksExisted + partialTick) / (float) EntityFlame.LIFESPAN;
        float size = (float) Math.pow(2 * alpha, 2);
        int i = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float) (i * 10) / 32F;
        float f5 = (float) (5 + i * 10) / 32F;
        float scale = 0.05625F * (0.8F + size);
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_((flame.prevRotationYaw + (flame.rotationYaw - flame.prevRotationYaw) * partialTick) - 90F));
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(flame.prevRotationPitch + (flame.rotationPitch - flame.prevRotationPitch) * partialTick));
        matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(45));
        matrix.func_227862_a_(scale, scale, scale);
        matrix.func_227861_a_(-4, 0, 0);

        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        IVertexBuilder builder = renderer.getBuffer(MekanismRenderType.renderFlame(getEntityTexture(flame)));
        float actualAlpha = 1 - alpha;
        for (int j = 0; j < 4; j++) {
            matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(90));
            builder.func_227887_a_(matrix.func_227866_c_().func_227872_b_(), 0, 0, scale);
            Matrix4f matrix4f = matrix.func_227866_c_().func_227870_a_();
            builder.func_227888_a_(matrix4f, -8, -2, 0).func_225583_a_(f2, f4).func_227885_a_(1, 1, 1, actualAlpha).endVertex();
            builder.func_227888_a_(matrix4f, 8, -2, 0).func_225583_a_(f3, f4).func_227885_a_(1, 1, 1, actualAlpha).endVertex();
            builder.func_227888_a_(matrix4f, 8, 2, 0).func_225583_a_(f3, f5).func_227885_a_(1, 1, 1, actualAlpha).endVertex();
            builder.func_227888_a_(matrix4f, -8, 2, 0).func_225583_a_(f2, f5).func_227885_a_(1, 1, 1, actualAlpha).endVertex();
        }
        MekanismRenderer.disableGlow(glowInfo);
        matrix.func_227865_b_();
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityFlame entity) {
        return Mekanism.rl("render/flame.png");
    }
}
