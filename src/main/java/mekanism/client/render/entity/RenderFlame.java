package mekanism.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderFlame extends EntityRenderer<EntityFlame> {

    public RenderFlame(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(@Nonnull EntityFlame entity, double x, double y, double z, float f, float partialTick) {
        if (entity.ticksExisted < 1) {
            return;
        }

        float alpha = (entity.ticksExisted + partialTick) / (float) EntityFlame.LIFESPAN;
        float size = (float) Math.pow(2 * alpha, 2);

        RenderSystem.pushMatrix();
        GlowInfo glowInfo = MekanismRenderer.enableGlow();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1, 1, 1, 1 - alpha);

        bindTexture(getEntityTexture(entity));

        RenderSystem.translatef((float) x, (float) y, (float) z);
        RenderSystem.rotatef((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick) - 90F, 0, 1, 0);
        RenderSystem.rotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0, 0, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();

        int i = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float) (i * 10) / 32F;
        float f5 = (float) (5 + i * 10) / 32F;
        float scale = 0.05625F * (0.8F + size);

        RenderSystem.enableRescaleNormal();
        RenderSystem.rotatef(45, 1, 0, 0);
        RenderSystem.scalef(scale, scale, scale);
        RenderSystem.translatef(-4F, 0, 0);

        for (int j = 0; j < 4; j++) {
            RenderSystem.rotatef(90, 1, 0, 0);
            RenderSystem.normal3f(0.0F, 0.0F, scale);

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-8.0D, -2.0D, 0.0D).tex(f2, f4).endVertex();
            worldrenderer.pos(8.0D, -2.0D, 0.0D).tex(f3, f4).endVertex();
            worldrenderer.pos(8.0D, 2.0D, 0.0D).tex(f3, f5).endVertex();
            worldrenderer.pos(-8.0D, 2.0D, 0.0D).tex(f2, f5).endVertex();
            tessellator.draw();
        }
        RenderSystem.disableRescaleNormal();
        MekanismRenderer.resetColor();
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        MekanismRenderer.disableGlow(glowInfo);
        RenderSystem.popMatrix();
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityFlame entity) {
        return new ResourceLocation(Mekanism.MODID, "render/flame.png");
    }
}
