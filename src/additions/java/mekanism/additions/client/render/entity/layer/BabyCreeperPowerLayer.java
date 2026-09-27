package mekanism.additions.client.render.entity.layer;

import mekanism.additions.client.model.BabyCreeperModel;
import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

public class BabyCreeperPowerLayer extends EnergySwirlLayer<CreeperRenderState, BabyCreeperModel> {

    private static final Identifier POWER_LOCATION = MekanismAdditions.rl("textures/entity/baby/creeper/creeper_armor.png");
    private final BabyCreeperModel model;

    public BabyCreeperPowerLayer(RenderLayerParent<CreeperRenderState, BabyCreeperModel> renderer, EntityModelSet entityModelSet) {
        super(renderer);
        model = new BabyCreeperModel(entityModelSet.bakeLayer(BabyModelLayers.BABY_CREEPER_ARMOR));
    }

    @Override
    protected boolean isPowered(CreeperRenderState state) {
        return state.isPowered;
    }

    @Override
    protected float xOffset(float t) {
        return t * 0.01F;
    }

    @Override
    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    @Override
    protected BabyCreeperModel model() {
        return this.model;
    }
}