package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.additions.client.model.BabyModelLayers;
import mekanism.additions.client.model.BabyCreeperModel;
import mekanism.additions.client.render.entity.layer.BabyCreeperPowerLayer;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/// Copy of vanilla's [creeper render][net.minecraft.client.renderer.entity.CreeperRenderer], modified to use our own model/layer that is properly scaled
public class BabyCreeperRenderer extends MobRenderer<EntityBabyCreeper, CreeperRenderState, BabyCreeperModel> {

    private static final Identifier CREEPER_LOCATION = MekanismAdditions.rl("textures/entity/baby/creeper/creeper.png");

    public BabyCreeperRenderer(EntityRendererProvider.Context context) {
        super(context, new BabyCreeperModel(context.getModelSet().bakeLayer(BabyModelLayers.BABY_CREEPER)), 0.5F);
        addLayer(new BabyCreeperPowerLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(CreeperRenderState state, PoseStack poseStack) {
        float swelling = state.swelling;
        float wobble = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
        swelling = Math.clamp(swelling, 0.0F, 1.0F);
        swelling *= swelling;
        swelling *= swelling;
        float s = (1.0F + swelling * 0.4F) * wobble;
        float hs = (1.0F + swelling * 0.1F) / wobble;
        poseStack.scale(s, hs, s);
    }

    @Override
    protected float getWhiteOverlayProgress(CreeperRenderState state) {
        return (int) (state.swelling * 10.0F) % 2 == 0 ? 0.0F : Math.clamp(state.swelling, 0.5F, 1.0F);
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return CREEPER_LOCATION;
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public void extractRenderState(EntityBabyCreeper entity, CreeperRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.swelling = entity.getSwelling(partialTicks);
        state.isPowered = entity.isPowered();
    }
}