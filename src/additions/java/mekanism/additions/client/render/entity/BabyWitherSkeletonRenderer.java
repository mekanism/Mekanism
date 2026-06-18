package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.WitherSkeletonRenderer] but with the model layer replaced
public class BabyWitherSkeletonRenderer extends AbstractSkeletonRenderer<EntityBabyWitherSkeleton, SkeletonRenderState> {

    private static final Identifier WITHER_SKELETON_LOCATION = Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

    public BabyWitherSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_WITHER_SKELETON, BabyModelLayers.BABY_WITHER_SKELETON_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return WITHER_SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }
}
