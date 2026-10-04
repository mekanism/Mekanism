package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

public class ScubaTankArmor implements ICustomArmor {

    public static final ScubaTankArmor SCUBA_TANK = new ScubaTankArmor();

    private ScubaTankArmor() {
    }

    @Override
    public <STATE extends HumanoidRenderState> boolean isVisible(HumanoidModel<STATE> baseModel, STATE state) {
        return baseModel.body.visible;
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, STATE state, boolean isBaby, ItemStack stack) {
        //TODO - 26.3: Reimplement this
        /*poseStack.pushPose();
        baseModel.body.translateAndRotate(poseStack);
        if (isBaby) {
            poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
            poseStack.translate(0, -0.2, 0);
        }
        poseStack.translate(-0.65, 1, 0.525);
        poseStack.scale(1.02F, -1.02F, -1.02F);
        leftFreeRunner.submitModel(collector, poseStack, lightCoords, state.outlineColor, stack.hasFoil());
        poseStack.popPose();*/
    }
}