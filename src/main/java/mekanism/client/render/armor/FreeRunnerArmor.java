package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.model.BaseModelCache.ItemModelHelper;
import mekanism.client.model.MekanismModelCache;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

public class FreeRunnerArmor implements ICustomArmor {

    public static final FreeRunnerArmor FREE_RUNNERS = new FreeRunnerArmor(MekanismModelCache.INSTANCE.LEFT_FREE_RUNNER, MekanismModelCache.INSTANCE.RIGHT_FREE_RUNNER);
    public static final FreeRunnerArmor ARMORED_FREE_RUNNERS = new FreeRunnerArmor(MekanismModelCache.INSTANCE.LEFT_FREE_RUNNER_ARMORED, MekanismModelCache.INSTANCE.RIGHT_FREE_RUNNER_ARMORED);

    private final ItemModelHelper leftFreeRunner;
    private final ItemModelHelper rightFreeRunner;

    private FreeRunnerArmor(ItemModelHelper leftFreeRunner, ItemModelHelper rightFreeRunner) {
        this.leftFreeRunner = leftFreeRunner;
        this.rightFreeRunner = rightFreeRunner;
    }

    @Override
    public <STATE extends HumanoidRenderState> boolean isVisible(HumanoidModel<STATE> baseModel, STATE state) {
        return baseModel.leftLeg.visible || baseModel.rightLeg.visible;
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, boolean isBaby, ItemStack stack) {
        boolean hasFoil = stack.hasFoil();
        if (baseModel.leftLeg.visible) {
            poseStack.pushPose();
            applyTransforms(poseStack, baseModel.leftLeg, isBaby, state, -0.65);
            leftFreeRunner.submitModel(nodeCollector, poseStack, lightCoords, state.outlineColor, hasFoil);
            poseStack.popPose();
        }
        if (baseModel.rightLeg.visible) {
            poseStack.pushPose();
            applyTransforms(poseStack, baseModel.rightLeg, isBaby, state, -0.375);
            rightFreeRunner.submitModel(nodeCollector, poseStack, lightCoords, state.outlineColor, hasFoil);
            poseStack.popPose();
        }
    }

    private void applyTransforms(PoseStack poseStack, ModelPart leg, boolean isBaby, HumanoidRenderState state, double xo) {
        leg.translateAndRotate(poseStack);
        if (isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
            poseStack.translate(0, -0.2, 0);
        }
        poseStack.translate(xo, 1, 0.525);
        poseStack.scale(1.02F, -1.02F, -1.02F);
    }
}