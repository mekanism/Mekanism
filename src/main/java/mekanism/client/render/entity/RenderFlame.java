package mekanism.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.entity.EntityFlame;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

public class RenderFlame extends EntityRenderer<EntityFlame> {

    public RenderFlame(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(EntityFlame flame, @Nonnull ClippingHelper camera, double camX, double camY, double camZ) {
        return flame.ticksExisted > 0 && super.shouldRender(flame, camera, camX, camY, camZ);
    }

    @Override
    public void render(@Nonnull EntityFlame flame, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        float alpha = (flame.ticksExisted + partialTick) / EntityFlame.LIFESPAN;
        float actualAlpha = 1 - alpha;
        if (actualAlpha <= 0) {
            return;
        }
        float size = (float) Math.pow(2 * alpha, 2);
        float f5 = 5 / 32F;
        float scale = 0.05625F * (0.8F + size);
        matrix.push();
        matrix.rotate(Vector3f.YP.rotationDegrees((flame.prevRotationYaw + (flame.rotationYaw - flame.prevRotationYaw) * partialTick) - 90F));
        matrix.rotate(Vector3f.ZP.rotationDegrees(flame.prevRotationPitch + (flame.rotationPitch - flame.prevRotationPitch) * partialTick));
        matrix.rotate(Vector3f.XP.rotationDegrees(45));
        matrix.scale(scale, scale, scale);
        matrix.translate(-4, 0, 0);
        IVertexBuilder builder = renderer.getBuffer(MekanismRenderType.renderFlame(getEntityTexture(flame)));
        for (int j = 0; j < 4; j++) {
            matrix.rotate(Vector3f.XP.rotationDegrees(90));
            builder.normal(matrix.getLast().getNormal(), 0, 0, scale);
            Matrix4f matrix4f = matrix.getLast().getMatrix();
            builder.pos(matrix4f, -8, -2, 0).color(1, 1, 1, actualAlpha).tex(0, 0).endVertex();
            builder.pos(matrix4f, 8, -2, 0).color(1, 1, 1, actualAlpha).tex(0.5F, 0).endVertex();
            builder.pos(matrix4f, 8, 2, 0).color(1, 1, 1, actualAlpha).tex(0.5F, f5).endVertex();
            builder.pos(matrix4f, -8, 2, 0).color(1, 1, 1, actualAlpha).tex(0, f5).endVertex();
        }
        matrix.pop();
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityFlame entity) {
        return MekanismUtils.getResource(ResourceType.RENDER, "flame.png");
    }
}
