package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelFreeRunners.FreeRunnerRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;

public class FreeRunnerArmor implements ICustomArmor, ResourceManagerReloadListener {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(false);
    public static final FreeRunnerArmor ARMORED_FREE_RUNNERS = new FreeRunnerArmor(true);

    private final boolean armored;
    private ModelFreeRunners model;

    private FreeRunnerArmor(boolean armored) {
        this.armored = armored;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (armored) {
            model = new ModelArmoredFreeRunners(Minecraft.getInstance().getEntityModels());
        } else {
            model = new ModelFreeRunners(Minecraft.getInstance().getEntityModels());
        }
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        //If the model isn't meant to be shown don't bother rendering anything
        if (!baseModel.leftLeg.visible && !baseModel.rightLeg.visible) {
            return;
        }
        poseStack.pushPose();
        if (state.isBaby) {
            float f1 = 1.0F / BABY_MODEL_TRANSFORM.babyBodyScale();
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0D, BABY_MODEL_TRANSFORM.bodyYOffset() / 16.0F, 0.0D);
        }
        FreeRunnerRenderState renderState = FreeRunnerRenderState.choose(baseModel.leftLeg.visible, baseModel.rightLeg.visible);
        if (baseModel.leftLeg.visible) {
            poseStack.pushPose();
            baseModel.leftLeg.translateAndRotate(poseStack);
            poseStack.translate(0, 0, 0.06);
            poseStack.scale(1.02F, 1.02F, 1.02F);
            poseStack.translate(-0.1375, -0.75, -0.0625);
            this.model.collect(renderState, poseStack, nodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, stack.hasFoil());
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}