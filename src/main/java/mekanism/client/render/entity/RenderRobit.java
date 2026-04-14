package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.entity.RenderRobit.RobitModelWrapper;
import mekanism.client.render.entity.RenderRobit.RobitRenderState;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderRobit extends MobRenderer<EntityRobit, RobitRenderState, RobitModelWrapper> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModelWrapper(), 0.5F);
    }

    @Override
    public RobitRenderState createRenderState() {
        return new RobitRenderState();
    }

    @Override
    public void extractRenderState(EntityRobit robit, RobitRenderState state, float partialTick) {
        super.extractRenderState(robit, state, partialTick);
        state.skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), robit.getSkin());
        state.modelData = robit.getModelData();
    }

    @Override
    public void submit(@NotNull RobitRenderState state, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector nodeCollector, @NotNull CameraRenderState camera) {
        if (state.skinLookup != null) {
            //TODO - 1.21.11: Replace the model here, and don't capture the state as part of setupAnim
            //this.model = new RobitModelWrapper(state);
            super.submit(state, poseStack, nodeCollector, camera);
        }
    }

    @NotNull
    @Override
    public Identifier getTextureLocation(@NotNull RobitRenderState state) {
        return RobitSpriteUploader.ATLAS_LOCATION;
    }

    public static class RobitRenderState extends LivingEntityRenderState {

        @Nullable
        public MekanismRobitSkins.SkinLookup skinLookup;
        public ModelData modelData = ModelData.EMPTY;
    }

    public static class RobitModelWrapper extends EntityModel<RobitRenderState> {

        @Nullable
        private RobitRenderState robit;

        RobitModelWrapper(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(@NotNull RobitRenderState robit) {
            this.robit = robit;
            super.setupAnim(robit);
        }

        @Override
        public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer builder, int light, int overlayLight, int color) {
            if (robit == null || robit.skinLookup == null) {
                //Setup didn't happen right
                return;
            }
            BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(robit.skinLookup);
            if (model == null) {
                //No model means we can't render (this shouldn't happen as we try to fall back to the default skin)
                Mekanism.logger.warn("Robit with skin: {} does not have a model. If this happened during a resource reload this can be ignored.", robit.skinLookup.identifier());
            } else {
                matrix.pushPose();
                matrix.mulPose(Axis.XP.rotationDegrees(180));
                matrix.translate(-0.5, -1.5, -0.5);
                PoseStack.Pose last = matrix.last();
                float red = ARGB.redFloat(color);
                float green = ARGB.greenFloat(color);
                float blue = ARGB.blueFloat(color);
                float alpha = ARGB.alphaFloat(color);
                for (BakedQuad quad : model.getQuads(null, null, robit.level().random, robit.modelData, null)) {
                    builder.putBulkData(last, quad, red, green, blue, alpha, light, overlayLight);
                }
                matrix.popPose();
            }
            //Clear current robit after rendering it
            robit = null;
        }
    }
}