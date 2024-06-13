package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.client.render.entity.layer.BabyCreeperChargeLayer;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/**
 * Copy of vanilla's creeper render, modified to use our own model/layer that is properly scaled
 */
public class RenderBabyCreeper extends MobRenderer<EntityBabyCreeper, ModelBabyCreeper> {

    private static final ResourceLocation CREEPER_TEXTURES = ResourceLocation.withDefaultNamespace("textures/entity/creeper/creeper.png");

    public RenderBabyCreeper(EntityRendererProvider.Context context) {
        super(context, new ModelBabyCreeper(context.getModelSet().bakeLayer(ModelBabyCreeper.CREEPER_LAYER)), 0.5F);
        this.addLayer(new BabyCreeperChargeLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(EntityBabyCreeper creeper, PoseStack matrix, float partialTicks) {
        float f = creeper.getSwelling(partialTicks);
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        matrix.scale(f2, f3, f2);
    }

    @Override
    protected float getWhiteOverlayProgress(EntityBabyCreeper creeper, float partialTicks) {
        float f = creeper.getSwelling(partialTicks);
        return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull EntityBabyCreeper entity) {
        return CREEPER_TEXTURES;
    }
}