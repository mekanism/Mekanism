package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.registries.AdditionsBlocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.TNTMinecartRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderObsidianTNTPrimed extends EntityRenderer<EntityObsidianTNT> {

    public RenderObsidianTNTPrimed(EntityRendererManager renderManager) {
        super(renderManager);
        shadowSize = 0.5F;
    }

    @Override
    public void func_225623_a_(@Nonnull EntityObsidianTNT tnt, float entityYaw, float partialTick, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light) {
        matrix.func_227860_a_();
        matrix.func_227861_a_(0, 0.5, 0);
        if ((float) tnt.getFuse() - partialTick + 1.0F < 10.0F) {
            float f = 1.0F - ((float) tnt.getFuse() - partialTick + 1.0F) / 10.0F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float f1 = 1.0F + f * 0.3F;
            matrix.func_227862_a_(f1, f1, f1);
        }

        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-90.0F));
        matrix.func_227861_a_(-0.5, -0.5, 0.5);
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90.0F));
        TNTMinecartRenderer.func_229127_a_(AdditionsBlocks.OBSIDIAN_TNT.getBlock().getDefaultState(), matrix, renderer, light, tnt.getFuse() / 5 % 2 == 0);
        matrix.func_227865_b_();
        super.func_225623_a_(tnt, entityYaw, partialTick, matrix, renderer, light);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityObsidianTNT entity) {
        return PlayerContainer.field_226615_c_;
    }
}