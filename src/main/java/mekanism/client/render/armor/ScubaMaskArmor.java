package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelScubaMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
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
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        if (!baseModel.head.visible) {
            //If the head model shouldn't show don't bother displaying it
            return;
        }
        poseStack.pushPose();
        if (state.isBaby) {
            if (baseModel.scaleHead) {
                float f = 1.5F / baseModel.babyHeadScale;
                poseStack.scale(f, f, f);
            }
            poseStack.translate(0.0D, baseModel.babyYHeadOffset / 16.0F, baseModel.babyZHeadOffset / 16.0F);
        }
        baseModel.head.translateAndRotate(poseStack);
        poseStack.translate(0, 0, 0.01);
        model.render(poseStack, renderer, lightCoords, OverlayTexture.NO_OVERLAY, stack.hasFoil());
        poseStack.popPose();
    }
}