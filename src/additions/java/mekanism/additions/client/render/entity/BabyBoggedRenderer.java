package mekanism.additions.client.render.entity;

import mekanism.additions.client.model.BabyBoggedModel;
import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.EntityBabyBogged;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.Identifier;

/// Copy of [net.minecraft.client.renderer.entity.BoggedRenderer] but with the model layer replaced
public class BabyBoggedRenderer extends AbstractSkeletonRenderer<EntityBabyBogged, BoggedRenderState> {

    private static final Identifier BOGGED_SKELETON_LOCATION = MekanismAdditions.rl("textures/entity/baby/skeleton/bogged.png");

    public BabyBoggedRenderer(EntityRendererProvider.Context context) {
        super(context, BabyModelLayers.BABY_BOGGED_ARMOR, new BabyBoggedModel(context.bakeLayer(BabyModelLayers.BABY_BOGGED)));
    }

    @Override
    public Identifier getTextureLocation(BoggedRenderState state) {
        return BOGGED_SKELETON_LOCATION;
    }

    @Override
    public BoggedRenderState createRenderState() {
        return new BoggedRenderState();
    }

    @Override
    public void extractRenderState(EntityBabyBogged entity, BoggedRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isSheared = entity.isSheared();
    }
}
