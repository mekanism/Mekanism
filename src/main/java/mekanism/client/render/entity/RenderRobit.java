package mekanism.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.client.RobitSpriteUploader;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.entity.RenderRobit.RobitModelWrapper;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismRobitSkins.SkinLookup;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RenderRobit extends MobRenderer<EntityRobit, RobitModelWrapper> {

    public RenderRobit(EntityRendererProvider.Context context) {
        super(context, new RobitModelWrapper(), 0.5F);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull EntityRobit robit) {
        return RobitSpriteUploader.ATLAS_LOCATION;
    }

    public static class RobitModelWrapper extends EntityModel<EntityRobit> {

        RobitModelWrapper() {
        }

        @Nullable
        private EntityRobit robit;

        @Override
        public void setupAnim(@NotNull EntityRobit robit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            this.robit = robit;
        }

        @Override
        public void renderToBuffer(@NotNull PoseStack matrix, @NotNull VertexConsumer builder, int light, int overlayLight, int color) {
            if (robit == null) {
                //Setup didn't happen right
                return;
            }
            SkinLookup skinLookup = MekanismRobitSkins.lookup(robit.level().registryAccess(), robit.getSkin());
            BakedModel model = MekanismModelCache.INSTANCE.getRobitSkin(skinLookup);
            if (model == null) {
                //No model means we can't render (this shouldn't happen as we try to fall back to the default skin)
                Mekanism.logger.warn("Robit with skin: {} does not have a model. If this happened during a resource reload this can be ignored.", skinLookup.location());
            } else {
                matrix.pushPose();
                matrix.mulPose(Axis.XP.rotationDegrees(180));
                matrix.translate(-0.5, -1.5, -0.5);
                PoseStack.Pose last = matrix.last();
                float red = MekanismRenderer.getRed(color);
                float green = MekanismRenderer.getGreen(color);
                float blue = MekanismRenderer.getBlue(color);
                float alpha = MekanismRenderer.getAlpha(color);
                for (BakedQuad quad : model.getQuads(null, null, robit.level().random, robit.getModelData(), null)) {
                    builder.putBulkData(last, quad, red, green, blue, alpha, light, overlayLight, false);
                }
                matrix.popPose();
            }
            //Clear current robit after rendering it
            robit = null;
        }
    }
}