package mekanism.additions.client.render.entity.layer;

import javax.annotation.Nonnull;
import mekanism.additions.client.model.ModelBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.EnergyLayer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.util.ResourceLocation;

public class BabyCreeperChargeLayer extends EnergyLayer<EntityBabyCreeper, ModelBabyCreeper> {

    private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final ModelBabyCreeper creeperModel = new ModelBabyCreeper(1);//Note: Use 1 instead of 2 for size

    public BabyCreeperChargeLayer(IEntityRenderer<EntityBabyCreeper, ModelBabyCreeper> renderer) {
        super(renderer);
    }

    @Override
    protected float xOffset(float modifier) {
        return modifier * 0.01F;
    }

    @Nonnull
    @Override
    protected ResourceLocation getTextureLocation() {
        return LIGHTNING_TEXTURE;
    }

    @Nonnull
    @Override
    protected EntityModel<EntityBabyCreeper> model() {
        return this.creeperModel;
    }
}