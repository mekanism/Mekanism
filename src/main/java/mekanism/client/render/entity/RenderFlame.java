package mekanism.client.render.entity;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.common.Mekanism;
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

@SideOnly(Side.CLIENT)
public class RenderFlame extends Render<EntityFlame> {

    public RenderFlame(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(@Nonnull EntityFlame entity, double x, double y, double z, float f, float partialTick) {
        if (entity.ticksExisted < 1) {
            return;
        }

        float alpha = (entity.ticksExisted + partialTick) / (float) EntityFlame.LIFESPAN;
        float size = (float) Math.pow(2 * alpha, 2);

        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).enableGlow().enableBlendPreset().colorAlpha(1 - alpha);

        bindTexture(getEntityTexture(entity));

        GlStateManager.translate(x, y, z);
        GlStateManager.rotate((entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTick) - 90F, 0, 1, 0);
        GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTick, 0, 0, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();

        int i = 0;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = (float) (i * 10) / 32F;
        float f5 = (float) (5 + i * 10) / 32F;
        float scale = 0.05625F * (0.8F + size);

        renderHelper.enableRescaleNormal();
        GlStateManager.rotate(45, 1, 0, 0);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(-4F, 0, 0);

        for (int j = 0; j < 4; j++) {
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.glNormal3f(0.0F, 0.0F, scale);

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(-8.0D, -2.0D, 0.0D).tex((double) f2, (double) f4).endVertex();
            worldrenderer.pos(8.0D, -2.0D, 0.0D).tex((double) f3, (double) f4).endVertex();
            worldrenderer.pos(8.0D, 2.0D, 0.0D).tex((double) f3, (double) f5).endVertex();
            worldrenderer.pos(-8.0D, 2.0D, 0.0D).tex((double) f2, (double) f5).endVertex();
            tessellator.draw();
        }
        renderHelper.cleanup();
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityFlame entity) {
        return new ResourceLocation(Mekanism.MODID, "render/Flame.png");
    }
}
