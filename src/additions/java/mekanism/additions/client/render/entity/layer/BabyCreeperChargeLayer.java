package mekanism.additions.client.render.entity.layer;

import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.ResourceLocation;

public class BabyCreeperChargeLayer extends EnergySwirlLayer<EntityBabyCreeper, ModelBabyCreeper> {

    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final ModelBabyCreeper creeperModel;

    public BabyCreeperChargeLayer(RenderLayerParent<EntityBabyCreeper, ModelBabyCreeper> renderer, EntityModelSet entityModelSet) {
        super(renderer);
        creeperModel = new ModelBabyCreeper(entityModelSet.bakeLayer(ModelBabyCreeper.ARMOR_LAYER));
    }

    @Override
    protected float xOffset(float modifier) {
        return modifier * 0.01F;
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return POWER_LOCATION;
    }

    @Nonnull
    @Override
    protected EntityModel<EntityBabyCreeper> model() {
        return this.creeperModel;
    }
}