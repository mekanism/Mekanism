package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.additions.common.AdditionsBlock;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderObsidianTNTPrimed extends EntityRenderer<EntityObsidianTNT> {

    public RenderObsidianTNTPrimed(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void doRender(@Nonnull EntityObsidianTNT entityobsidiantnt, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher renderer = Minecraft.getInstance().getBlockRendererDispatcher();
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) x, (float) y + 0.5F, (float) z);

        if (entityobsidiantnt.getFuse() - partialTicks + 1.0F < 10.0F) {
            float f = 1.0F - (entityobsidiantnt.getFuse() - partialTicks + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float scale = 1.0F + f * 0.3F;
            RenderSystem.scalef(scale, scale, scale);
        }

        float f3 = (1.0F - ((entityobsidiantnt.getFuse() - partialTicks) + 1.0F) / 100F) * 0.8F;
        bindEntityTexture(entityobsidiantnt);
        RenderSystem.translatef(-0.5F, -0.5F, 0.5F);
        renderer.renderBlockBrightness(AdditionsBlock.OBSIDIAN_TNT.getBlock().getDefaultState(), entityobsidiantnt.getBrightness());
        RenderSystem.translatef(0, 0, 1.0F);

        if (entityobsidiantnt.getFuse() / 5 % 2 == 0) {
            RenderSystem.disableTexture();
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.DST_ALPHA);
            RenderSystem.color4f(1, 1, 1, f3);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            renderer.renderBlockBrightness(AdditionsBlock.OBSIDIAN_TNT.getBlock().getDefaultState(), 1.0F);
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
            RenderSystem.enableLighting();
            RenderSystem.enableTexture();
        }

        RenderSystem.popMatrix();
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityObsidianTNT entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}