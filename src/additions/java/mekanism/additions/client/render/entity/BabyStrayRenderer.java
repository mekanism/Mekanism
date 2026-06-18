package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.StrayRenderer] but with the model layer replaced
public class BabyStrayRenderer extends AbstractSkeletonRenderer<EntityBabyStray, SkeletonRenderState> {

    private static final Identifier STRAY_SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final Identifier STRAY_CLOTHES_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");

    public BabyStrayRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_STRAY, BabyModelLayers.BABY_STRAY_ARMOR);
        this.addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), BabyModelLayers.BABY_STRAY_OUTER_LAYER, STRAY_CLOTHES_LOCATION));
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return STRAY_SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
