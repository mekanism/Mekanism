package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.registries.AdditionsBlocks;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

public class RenderObsidianTNTPrimed extends EntityRenderer<EntityObsidianTNT> {

    public RenderObsidianTNTPrimed(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void doRender(@Nonnull EntityObsidianTNT entityobsidiantnt, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x, (float) y + 0.5F, (float) z);

        if (entityobsidiantnt.getFuse() - partialTicks + 1.0F < 10.0F) {
            float f = 1.0F - (entityobsidiantnt.getFuse() - partialTicks + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float scale = 1.0F + f * 0.3F;
            GlStateManager.scalef(scale, scale, scale);
        }

        float f3 = (1.0F - ((entityobsidiantnt.getFuse() - partialTicks) + 1.0F) / 100F) * 0.8F;
        bindEntityTexture(entityobsidiantnt);
        GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
        renderer.renderBlockBrightness(AdditionsBlocks.OBSIDIAN_TNT.getBlock().getDefaultState(), entityobsidiantnt.getBrightness());
        GlStateManager.translatef(0, 0, 1.0F);

        if (entityobsidiantnt.getFuse() / 5 % 2 == 0) {
            GlStateManager.disableTexture();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.DST_ALPHA);
            GlStateManager.color4f(1, 1, 1, f3);
            GlStateManager.polygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            renderer.renderBlockBrightness(AdditionsBlocks.OBSIDIAN_TNT.getBlock().getDefaultState(), 1.0F);
            GlStateManager.polygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            MekanismRenderer.resetColor();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture();
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityObsidianTNT entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}