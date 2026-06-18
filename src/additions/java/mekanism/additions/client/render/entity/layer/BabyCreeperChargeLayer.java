package mekanism.additions.client.render.entity.layer;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.client.model.ModelBabyCreeper;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public class BabyCreeperChargeLayer extends EnergySwirlLayer<CreeperRenderState, ModelBabyCreeper> {

    private static final Identifier POWER_LOCATION = Identifier.withDefaultNamespace("textures/entity/creeper/creeper_armor.png");
    private final ModelBabyCreeper creeperModel;

    public BabyCreeperChargeLayer(RenderLayerParent<CreeperRenderState, ModelBabyCreeper> renderer, EntityModelSet entityModelSet) {
        super(renderer);
        creeperModel = new ModelBabyCreeper(entityModelSet.bakeLayer(BabyModelLayers.BABY_CREEPER_ARMOR));
    }

    @Override
    protected float xOffset(float modifier) {
        return modifier * 0.01F;
    }

    @Override
    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    @Override
    protected boolean isPowered(CreeperRenderState state) {
        return state.isPowered;
    }

    @Override
    protected ModelBabyCreeper model() {
        return this.creeperModel;
    }
}