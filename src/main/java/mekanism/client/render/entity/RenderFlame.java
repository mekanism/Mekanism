package mekanism.client.render.entity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.entity.EntityFlame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

@SideOnly(Side.CLIENT)
public class RenderFlame extends Render<EntityFlame> {

    public RenderFlame(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityFlame entity, double x, double y, double z, float f, float partialTick) {
        if (entity.ticksExisted < 1) {
            return;
        }

        float alpha = (entity.ticksExisted + partialTick) / (float) EntityFlame.LIFESPAN;
        float size = (float) Math.pow(2 * alpha, 2);

        GlStateManager.pushMatrix();
        MekanismRenderer.glowOn();
        MekanismRenderer.blendOn();
        GL11.glColor4f(1, 1, 1, 1 - alpha);

        bindTexture(getEntityTexture(entity));

        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager
              .rotate((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick) - 90F,
                    0.0F, 1.0F, 0.0F);
        GlStateManager
              .rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0.0F,
                    0.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();

        int i = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float) (0 + i * 10) / 32F;
        float f5 = (float) (5 + i * 10) / 32F;
        float scale = 0.05625F * (0.8F + size);

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GlStateManager.rotate(45F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-4F, 0.0F, 0.0F);

        for (int j = 0; j < 4; j++) {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, scale);

            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-8.0D, -2.0D, 0.0D).tex((double) f2, (double) f4).endVertex();
            worldrenderer.pos(8.0D, -2.0D, 0.0D).tex((double) f3, (double) f4).endVertex();
            worldrenderer.pos(8.0D, 2.0D, 0.0D).tex((double) f3, (double) f5).endVertex();
            worldrenderer.pos(-8.0D, 2.0D, 0.0D).tex((double) f2, (double) f5).endVertex();
            tessellator.draw();
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        MekanismRenderer.resetColor();
        MekanismRenderer.glowOff();
        MekanismRenderer.blendOff();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFlame entity) {
        return new ResourceLocation("mekanism:render/Flame.png");
    }
}
