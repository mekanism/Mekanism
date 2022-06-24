package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScubaMaskArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final ScubaMaskArmor SCUBA_MASK = new ScubaMaskArmor();

    private ModelScubaMask model;

    private ScubaMaskArmor() {
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        model = new ModelScubaMask(Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void render(HumanoidModel<? extends LivingEntity> baseModel, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack) {
        if (!baseModel.head.visible) {
            //If the head model shouldn't show don't bother displaying it
            return;
        }
        if (baseModel.young) {
            matrix.pushPose();
            if (baseModel.scaleHead) {
                float f = 1.5F / baseModel.babyHeadScale;
                matrix.scale(f, f, f);
            }
            matrix.translate(0.0D, baseModel.babyYHeadOffset / 16.0F, baseModel.babyZHeadOffset / 16.0F);
            renderMask(baseModel, matrix, renderer, light, overlayLight, hasEffect);
            matrix.popPose();
        } else {
            renderMask(baseModel, matrix, renderer, light, overlayLight, hasEffect);
        }
    }

    private void renderMask(HumanoidModel<? extends LivingEntity> baseModel, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light,
          int overlayLight, boolean hasEffect) {
        matrix.pushPose();
        baseModel.head.translateAndRotate(matrix);
        matrix.translate(0, 0, 0.01);
        model.render(matrix, renderer, light, overlayLight, hasEffect);
        matrix.popPose();
    }
}