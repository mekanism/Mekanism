package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.StrayRenderer] but with the model layer replaced
public class BabyStrayRenderer extends AbstractSkeletonRenderer<EntityBabyStray, SkeletonRenderState> {

    private static final Identifier STRAY_SKELETON_LOCATION = MekanismAdditions.rl("textures/entity/baby/skeleton/stray.png");

    public BabyStrayRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_STRAY, BabyModelLayers.BABY_STRAY_ARMOR);
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
