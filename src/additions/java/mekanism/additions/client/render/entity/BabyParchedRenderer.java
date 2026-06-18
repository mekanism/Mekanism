package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.entity.baby.EntityBabyParched;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.ParchedRenderer] but with the model layer replaced
public class BabyParchedRenderer extends AbstractSkeletonRenderer<EntityBabyParched, SkeletonRenderState> {

    private static final Identifier PARCHED_SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/parched.png");

    public BabyParchedRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_PARCHED, BabyModelLayers.BABY_PARCHED_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return PARCHED_SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
