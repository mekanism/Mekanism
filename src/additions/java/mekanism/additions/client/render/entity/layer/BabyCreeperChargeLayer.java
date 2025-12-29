package mekanism.additions.client.render.entity.layer;

import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class BabyCreeperChargeLayer extends EnergySwirlLayer<EntityBabyCreeper, ModelBabyCreeper> {

    private static final Identifier POWER_LOCATION = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    private final ModelBabyCreeper creeperModel;

    public BabyCreeperChargeLayer(RenderLayerParent<EntityBabyCreeper, ModelBabyCreeper> renderer, EntityModelSet entityModelSet) {
        super(renderer);
        creeperModel = new ModelBabyCreeper(entityModelSet.bakeLayer(ModelBabyCreeper.ARMOR_LAYER));
    }

    @Override
    protected float xOffset(float modifier) {
        return modifier * 0.01F;
    }

    @NotNull
    @Override
    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    @NotNull
    @Override
    protected EntityModel<EntityBabyCreeper> model() {
        return this.creeperModel;
    }
}