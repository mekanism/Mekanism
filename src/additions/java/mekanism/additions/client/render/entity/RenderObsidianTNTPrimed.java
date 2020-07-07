package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.registries.AdditionsBlocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.TNTMinecartRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class RenderObsidianTNTPrimed extends EntityRenderer<EntityObsidianTNT> {

    public RenderObsidianTNTPrimed(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void render(@Nonnull EntityObsidianTNT tnt, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.push();
        matrix.translate(0, 0.5, 0);
        if (tnt.getFuse() - partialTick + 1.0F < 10.0F) {
            float f = 1.0F - (tnt.getFuse() - partialTick + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float f1 = 1.0F + f * 0.3F;
            matrix.scale(f1, f1, f1);
        }

        matrix.rotate(Vector3f.YP.rotationDegrees(-90.0F));
        matrix.translate(-0.5, -0.5, 0.5);
        matrix.rotate(Vector3f.YP.rotationDegrees(90.0F));
        TNTMinecartRenderer.renderTntFlash(AdditionsBlocks.OBSIDIAN_TNT.getBlock().getDefaultState(), matrix, renderer, light, tnt.getFuse() / 5 % 2 == 0);
        matrix.pop();
        super.render(tnt, entityYaw, partialTick, matrix, renderer, light);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityObsidianTNT entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }
}