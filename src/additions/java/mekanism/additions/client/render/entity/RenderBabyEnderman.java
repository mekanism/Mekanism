package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.additions.client.model.ModelBabyEnderman;
import mekanism.additions.client.render.entity.layer.BabyEndermanEyesLayer;
import mekanism.additions.client.render.entity.layer.BabyEndermanHeldBlockLayer;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * Copy of vanilla's enderman render, modified to use our own model/layer that is properly scaled, so that the block is held in the correct spot and the head is in the
 * proper place.
 */
public class RenderBabyEnderman extends MobRenderer<EntityBabyEnderman, ModelBabyEnderman> {

    private static final ResourceLocation ENDERMAN_TEXTURES = ResourceLocation.withDefaultNamespace("textures/entity/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();

    public RenderBabyEnderman(EntityRendererProvider.Context context) {
        super(context, new ModelBabyEnderman(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
        this.addLayer(new BabyEndermanEyesLayer(this));
        this.addLayer(new BabyEndermanHeldBlockLayer(this));
    }

    @Override
    public void render(EntityBabyEnderman enderman, float entityYaw, float partialTicks, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int packedLightIn) {
        ModelBabyEnderman model = getModel();
        model.carrying = enderman.getCarriedBlock() != null;
        model.creepy = enderman.isCreepy();
        super.render(enderman, entityYaw, partialTicks, matrix, renderer, packedLightIn);
    }

    @NotNull
    @Override
    public Vec3 getRenderOffset(EntityBabyEnderman enderman, float partialTicks) {
        if (enderman.isCreepy()) {
            double offset = 0.02 * enderman.getScale();
            return new Vec3(this.random.nextGaussian() * offset, 0, this.random.nextGaussian() * offset);
        }
        return super.getRenderOffset(enderman, partialTicks);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull EntityBabyEnderman enderman) {
        return ENDERMAN_TEXTURES;
    }
}