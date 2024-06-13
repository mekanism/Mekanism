package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.entity.EntityFlame;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class RenderFlame extends EntityRenderer<EntityFlame> {

    public RenderFlame(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(EntityFlame flame, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return flame.tickCount > 0 && super.shouldRender(flame, camera, camX, camY, camZ);
    }

    @Override
    public void render(@NotNull EntityFlame flame, float entityYaw, float partialTick, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light) {
        float alpha = (flame.tickCount + partialTick) / EntityFlame.LIFESPAN;
        float actualAlpha = 1 - alpha;
        if (actualAlpha <= 0) {
            return;
        }
        float size = (float) Math.pow(2 * alpha, 2);
        float f5 = 5 / 32F;
        float scale = 0.05625F * (0.8F + size);
        int alphaColor = (int) (actualAlpha * 255);
        matrix.pushPose();
        matrix.mulPose(Axis.YP.rotationDegrees((flame.yRotO + (flame.getYRot() - flame.yRotO) * partialTick) - 90F));
        matrix.mulPose(Axis.ZP.rotationDegrees(flame.xRotO + (flame.getXRot() - flame.xRotO) * partialTick));
        matrix.mulPose(Axis.XP.rotationDegrees(45));
        matrix.scale(scale, scale, scale);
        matrix.translate(-4, 0, 0);
        VertexConsumer builder = renderer.getBuffer(MekanismRenderType.FLAME.apply(getTextureLocation(flame)));
        for (int j = 0; j < 4; j++) {
            matrix.mulPose(Axis.XP.rotationDegrees(90));
            builder.setNormal(matrix.last(), 0, 0, scale);
            Matrix4f matrix4f = matrix.last().pose();
            builder.addVertex(matrix4f, -8, -2, 0)
                  .setUv(0, 0)
                  .setColor(0xFF, 0xFF, 0xFF, alphaColor);
            builder.addVertex(matrix4f, 8, -2, 0)
                  .setUv(0.5F, 0)
                  .setColor(0xFF, 0xFF, 0xFF, alphaColor);
            builder.addVertex(matrix4f, 8, 2, 0)
                  .setUv(0.5F, f5)
                  .setColor(0xFF, 0xFF, 0xFF, alphaColor);
            builder.addVertex(matrix4f, -8, 2, 0)
                  .setUv(0, f5)
                  .setColor(0xFF, 0xFF, 0xFF, alphaColor);
        }
        matrix.popPose();
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull EntityFlame entity) {
        return MekanismUtils.getResource(ResourceType.RENDER, "flame.png");
    }
}
