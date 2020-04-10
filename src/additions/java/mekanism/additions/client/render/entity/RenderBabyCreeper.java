package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.client.render.entity.layer.BabyCreeperChargeLayer;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Copy of vanilla's creeper render, modified to use our own model/layer that is properly scaled
 */
public class RenderBabyCreeper extends MobRenderer<EntityBabyCreeper, ModelBabyCreeper> {

    private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");

    public RenderBabyCreeper(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new ModelBabyCreeper(), 0.5F);
        this.addLayer(new BabyCreeperChargeLayer(this));
    }

    @Override
    protected void preRenderCallback(EntityBabyCreeper creeper, MatrixStack matrix, float partialTicks) {
        float f = creeper.getCreeperFlashIntensity(partialTicks);
        float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        matrix.scale(f2, f3, f2);
    }

    @Override
    protected float getOverlayProgress(EntityBabyCreeper creeper, float partialTicks) {
        float f = creeper.getCreeperFlashIntensity(partialTicks);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : MathHelper.clamp(f, 0.5F, 1.0F);
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull EntityBabyCreeper entity) {
        return CREEPER_TEXTURES;
    }
}