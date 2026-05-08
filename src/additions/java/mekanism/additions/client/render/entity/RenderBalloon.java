package mekanism.additions.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import mekanism.additions.client.model.AdditionsModelCache;
import mekanism.additions.client.render.entity.RenderBalloon.BalloonRenderState;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.BaseModelCache;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderBalloon extends EntityRenderer<EntityBalloon, BalloonRenderState> {

    private static final RenderType RENDER_TYPE = Sheets.translucentBlockSheet();

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
        state.tint[0] = balloon.getColor().getPackedColor();
        boolean latched = balloon.isLatched();
        if (balloon.isLatchedToEntity()) {
            //Shift the rendering of the balloon to be over the entity
            Vec3 latchedLerp = Mth.lerp(partialTick, balloon.latchedEntity.oldPosition(), balloon.latchedEntity.position());
            Vec3 balloonLerp = Mth.lerp(partialTick, balloon.oldPosition(), balloon.position());
            state.latchedAdjustment = latchedLerp.subtract(balloonLerp);
        }
        BaseModelCache.JSONModelData model = latched ? AdditionsModelCache.INSTANCE.BALLOON : AdditionsModelCache.INSTANCE.BALLOON_FREE;
        state.model = model.getBakedModel();
    }

    @Override
    public void submit(BalloonRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(-0.5, -1, -0.5);

        if (state.latchedAdjustment != null) {
            poseStack.translate(state.latchedAdjustment);
        }

        nodeCollector.submitBlockModel(poseStack, RENDER_TYPE, state.model, state.tint, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, camera);
    }

    @Override
    protected boolean affectedByCulling(EntityBalloon balloon) {
        return false;
    }

    public static class BalloonRenderState extends EntityRenderState {

        @Nullable
        public Vec3 latchedAdjustment;
        public List<BlockStateModelPart> model = Collections.emptyList();
        public int[] tint = new int[1];
    }
}