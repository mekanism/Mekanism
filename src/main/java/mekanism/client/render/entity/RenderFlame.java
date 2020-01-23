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
    public void render(@Nonnull EntityFlame flame, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
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
        matrix.push();
        matrix.rotate(Vector3f.YP.rotationDegrees((flame.prevRotationYaw + (flame.rotationYaw - flame.prevRotationYaw) * partialTick) - 90F));
        matrix.rotate(Vector3f.ZP.rotationDegrees(flame.prevRotationPitch + (flame.rotationPitch - flame.prevRotationPitch) * partialTick));
        matrix.rotate(Vector3f.XP.rotationDegrees(45));
        matrix.scale(scale, scale, scale);
        matrix.translate(-4, 0, 0);

        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        IVertexBuilder builder = renderer.getBuffer(MekanismRenderType.renderFlame(getEntityTexture(flame)));
        float actualAlpha = 1 - alpha;
        for (int j = 0; j < 4; j++) {
            matrix.rotate(Vector3f.XP.rotationDegrees(90));
            builder.normal(matrix.getLast().getNormalMatrix(), 0, 0, scale);
            Matrix4f matrix4f = matrix.getLast().getPositionMatrix();
            builder.pos(matrix4f, -8, -2, 0).tex(f2, f4).color(1, 1, 1, actualAlpha).endVertex();
            builder.pos(matrix4f, 8, -2, 0).tex(f3, f4).color(1, 1, 1, actualAlpha).endVertex();
            builder.pos(matrix4f, 8, 2, 0).tex(f3, f5).color(1, 1, 1, actualAlpha).endVertex();
            builder.pos(matrix4f, -8, 2, 0).tex(f2, f5).color(1, 1, 1, actualAlpha).endVertex();
        }
        MekanismRenderer.disableGlow(glowInfo);
        matrix.pop();
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityFlame entity) {
        return Mekanism.rl("render/flame.png");
    }
}
