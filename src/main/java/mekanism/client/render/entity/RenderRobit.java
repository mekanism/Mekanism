package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collections;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.robit.RobitSkinManager;
import mekanism.client.model.robit.RobitSkinManager.BakeResult;
import mekanism.client.render.entity.RenderRobit.RobitRenderState;
import mekanism.common.entity.EntityRobit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class RenderRobit extends MobRenderer<EntityRobit, RobitRenderState, EntityModel<RobitRenderState>> {

    private static final ModelPart EMPTY_MODEL_PART = new ModelPart(Collections.emptyList(), Collections.emptyMap());
    private static final EntityModel<RobitRenderState> EMPTY_MODEL = new EntityModel<>(
          EMPTY_MODEL_PART,
          _ -> {
              throw new UnsupportedOperationException("Should not be rendered");
          }
    ) {};

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, EMPTY_MODEL, 0.5F);
    }

    @Override
    @Nullable
    protected RenderType getRenderType(RobitRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        return null;//prevent super.render from rendering a model
    }

    @Override
    public RobitRenderState createRenderState() {
        return new RobitRenderState();
    }

    @Override
    public void extractRenderState(EntityRobit robit, RobitRenderState state, float partialTick) {
        super.extractRenderState(robit, state, partialTick);
        state.model = RobitSkinManager.get().getBaked(robit.getSkin(), robit.getModelTexture());
    }

    @Override
    public void submit(RobitRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState camera) {
        boolean isBodyVisible = this.isBodyVisible(state);
        boolean forceTransparent = !isBodyVisible && !state.isInvisibleToPlayer;
        //nb: this segment copied from super, bed & layer checks removed, invisibility check moved, and model submit replaced with submitRobitSkin()
        if (!forceTransparent) {
            poseStack.pushPose();
            {
                float scale = state.scale;
                poseStack.scale(scale, scale, scale);
                this.setupRotations(state, poseStack, state.bodyRot, scale);
                poseStack.scale(-1.0F, -1.0F, 1.0F);
                this.scale(state, poseStack);
                poseStack.translate(0.0F, -1.501F, 0.0F);
                //isBodyVisible & forceTransparent moved above
                submitRobitSkin(state, poseStack, nodeCollector);
            }
            poseStack.popPose();
        }
        //allow any layers/nametag/leash states to submit
        super.submit(state, poseStack, nodeCollector, camera);
    }

    private void submitRobitSkin(RobitRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(-0.5, -1.5, -0.5);
        submitRobitSkin(state.model, poseStack, nodeCollector, getOverlayCoords(state, this.getWhiteOverlayProgress(state)), state.lightCoords, state.outlineColor);
        poseStack.popPose();
    }

    public static void submitRobitSkin(BakeResult baked, PoseStack poseStack, SubmitNodeCollector nodeCollector, int overlay, int lightCoords, int outlineColor) {
        nodeCollector.submitBlockModel(poseStack, baked.renderType(), baked.model(), BlockModelRenderState.EMPTY_TINTS, lightCoords, overlay, outlineColor);
    }

    @Override
    public Identifier getTextureLocation(RobitRenderState state) {
        return RobitSpriteUploader.ATLAS_LOCATION;
    }

    public static class RobitRenderState extends LivingEntityRenderState {

        public BakeResult model = RobitSkinManager.get().getMissing();
    }
}