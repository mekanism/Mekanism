package mekanism.client.render.entity;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismBlocks;
import mekanism.common.entity.EntityObsidianTNT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderObsidianTNTPrimed extends Render<EntityObsidianTNT> {

    public RenderObsidianTNTPrimed(RenderManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityObsidianTNT entityobsidiantnt, double x, double y, double z, float entityYaw,
          float partialTicks) {
        BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5F, (float) z);

        if (entityobsidiantnt.fuse - partialTicks + 1.0F < 10.0F) {
            float f = 1.0F - (entityobsidiantnt.fuse - partialTicks + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float f1 = 1.0F + f * 0.3F;
            GlStateManager.scale(f1, f1, f1);
        }

        float f3 = (1.0F - ((entityobsidiantnt.fuse - partialTicks) + 1.0F) / 100F) * 0.8F;
        bindEntityTexture(entityobsidiantnt);
        GlStateManager.translate(-0.5F, -0.5F, 0.5F);
        renderer.renderBlockBrightness(MekanismBlocks.ObsidianTNT.getDefaultState(), entityobsidiantnt.getBrightness());
        GlStateManager.translate(0.0F, 0.0F, 1.0F);

        if (entityobsidiantnt.fuse / 5 % 2 == 0) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f3);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            renderer.renderBlockBrightness(MekanismBlocks.ObsidianTNT.getDefaultState(), 1.0F);
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            MekanismRenderer.resetColor();
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityObsidianTNT entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
