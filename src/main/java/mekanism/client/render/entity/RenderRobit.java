package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.robit.RobitSkin;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.entity.RenderRobit.RobitModelWrapper;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class RenderRobit extends MobRenderer<EntityRobit, RobitModelWrapper> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModelWrapper(), 0.5F);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull EntityRobit robit) {
        return RobitSpriteUploader.ATLAS_LOCATION;
    }

    public static class RobitModelWrapper extends EntityModel<EntityRobit> {

        RobitModelWrapper() {
        }

        @Nullable
        private EntityRobit robit;

        @Override
        public void setupAnim(@Nonnull EntityRobit robit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            this.robit = robit;
        }

        @Override
        public void renderToBuffer(@Nonnull PoseStack matrix, @Nonnull VertexConsumer builder, int light, int overlayLight, float red, float green, float blue,
              float alpha) {
            if (robit == null) {
                //Setup didn't happen right
                return;
            }
            RobitSkin skin = robit.getSkin();
            BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skin);
            if (model == null) {
                //No model means we can't render (this shouldn't happen as we try to fall back to the default skin)
                Mekanism.logger.warn("Robit with skin: {} does not have a model.", skin.getRegistryName());
            } else {
                matrix.pushPose();
                matrix.mulPose(Vector3f.XP.rotationDegrees(180));
                matrix.translate(-0.5, -1.5, -0.5);
                PoseStack.Pose last = matrix.last();
                for (BakedQuad quad : model.getQuads(null, null, robit.level.random, robit.getModelData())) {
                    builder.putBulkData(last, quad, red, green, blue, alpha, light, overlayLight);
                }
                matrix.popPose();
            }
            //Clear current robit after rendering it
            robit = null;
        }
    }
}