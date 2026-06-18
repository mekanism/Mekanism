package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.SkeletonRenderer] but with the model layer replaced
public class BabySkeletonRenderer extends AbstractSkeletonRenderer<EntityBabySkeleton, SkeletonRenderState> {

    private static final Identifier SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");

    public BabySkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_SKELETON, BabyModelLayers.BABY_SKELETON_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
