package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.entity.RenderFlame.FlameRenderState;
import mekanism.common.entity.EntityFlame;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

public class RenderFlame extends EntityRenderer<EntityFlame, FlameRenderState> {

    private static final Identifier TEXTURE = MekanismUtils.getResource(ResourceType.RENDER, "flame.png");

    private final FlameModel model;

    public RenderFlame(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlameModel(context.bakeLayer(FlameModel.FLAME_LAYER));
    }

    @Override
    public FlameRenderState createRenderState() {
        return new FlameRenderState();
    }

    @Override
    public void extractRenderState(EntityFlame flame, FlameRenderState state, float partialTick) {
        super.extractRenderState(flame, state, partialTick);
        state.xRot = flame.getXRot(partialTick);
        state.yRot = flame.getYRot(partialTick);
        float alpha = (flame.tickCount + partialTick) / EntityFlame.LIFESPAN;
        float actualAlpha = 1 - alpha;
        state.tintColor = ARGB.white(ARGB.as8BitChannel(actualAlpha));
        //TODO - 1.21.11: Figure out this scaling and if we even want it
        float size = (float) Math.pow(2 * alpha, 2);
        //state.scale = 0.8F + size;
    }

    @Override
    public void submit(@NotNull FlameRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector, @NotNull CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

        poseStack.scale(state.scale, state.scale, state.scale);

        nodeCollector.submitModel(
              this.model,
              state,
              poseStack,
              MekanismRenderType.FLAME.apply(TEXTURE),
              state.lightCoords,
              OverlayTexture.NO_OVERLAY,
              state.tintColor,
              null,
              state.outlineColor,
              null
        );
        poseStack.popPose();
        super.submit(state, poseStack, nodeCollector, camera);
    }

    @Override
    public boolean shouldRender(EntityFlame flame, @NotNull Frustum camera, double camX, double camY, double camZ) {
        float alpha = flame.tickCount / (float) EntityFlame.LIFESPAN;
        float actualAlpha = 1 - alpha;
        return actualAlpha > 0 && super.shouldRender(flame, camera, camX, camY, camZ);
    }

    public class FlameRenderState extends EntityRenderState {

        public float xRot;
        public float yRot;
        public int tintColor = -1;
        public float scale = 1;
    }
}
