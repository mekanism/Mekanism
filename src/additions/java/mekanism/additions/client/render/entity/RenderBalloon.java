package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.client.render.entity.RenderBalloon.BalloonRenderState;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.BaseModelCache.JSONModelData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderBalloon extends EntityRenderer<EntityBalloon, BalloonRenderState> {

    //TODO - 1.21.11: Figure out how to specify the texture
    public static final Identifier BALLOON_TEXTURE = MekanismAdditions.rl("textures/item/balloon.png");

    public RenderBalloon(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BalloonRenderState createRenderState() {
        return new BalloonRenderState();
    }

    @Override
    public void extractRenderState(EntityBalloon balloon, BalloonRenderState state, float partialTick) {
        super.extractRenderState(balloon, state, partialTick);
        state.balloonTint = balloon.getColor().getPackedColor();
        state.latched = balloon.isLatched();
        if (balloon.isLatchedToEntity()) {
            //Shift the rendering of the balloon to be over the entity
            Vec3 latchedLerp = Mth.lerp(partialTick, balloon.latchedEntity.oldPosition(), balloon.latchedEntity.position());
            Vec3 balloonLerp = Mth.lerp(partialTick, balloon.oldPosition(), balloon.position());
            state.latchedAdjustment = latchedLerp.subtract(balloonLerp);
        }
    }

    @Override
    public void submit(BalloonRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(-0.5, -1, -0.5);

        if (state.latchedAdjustment != null) {
            poseStack.translate(state.latchedAdjustment);
        }

        JSONModelData model = state.latched ? AdditionsModelCache.INSTANCE.BALLOON : AdditionsModelCache.INSTANCE.BALLOON_FREE;

        List<BakedQuad> quads = model.getQuads(balloon.level().random);
        RenderType renderType = RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
        VertexConsumer builder = renderer.getBuffer(renderType);
        PoseStack.Pose last = poseStack.last();
        for (BakedQuad quad : quads) {
            int color = quad.tintIndex() == 0 ? state.balloonTint : 0xFFFFFFFF;
            builder.putBulkData(last, quad, ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), ARGB.alphaFloat(color), state.lightCoords, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, camera);
    }

    @Override
    protected boolean affectedByCulling(EntityBalloon balloon) {
        return false;
    }

    public static class BalloonRenderState extends EntityRenderState {

        public int balloonTint = 0xFFFFFFFF;
        @Nullable
        public Vec3 latchedAdjustment;
        public boolean latched;
    }
}