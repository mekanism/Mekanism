package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.common.Mekanism;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;

public class ChestArmor implements ICustomArmor {

    public static final ContextKey<ItemStackRenderState> CHEST_CONTEXT = new ContextKey<>(Mekanism.rl("chest"));
    //TODO: See if we can replace having separate ones like this with just having a custom ItemDisplayContext that is defined in the json
    // and parsed in ClientRegistration#registerRenderStateModifiers
    public static final ChestArmor JETPACK = new ChestArmor(true);
    public static final ChestArmor SCUBA_TANK = new ChestArmor(false);

    private final boolean jetpack;

    private ChestArmor(boolean jetpack) {
        this.jetpack = jetpack;
    }

    @Override
    public <STATE extends HumanoidRenderState> boolean isVisible(HumanoidModel<STATE> baseModel, STATE state) {
        return baseModel.body.visible;
    }

    @Override
    public <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, STATE state,
          boolean isBaby, ItemStack stack) {
        ItemStackRenderState bodyItem = state.getRenderData(CHEST_CONTEXT);
        if (bodyItem != null) {
            poseStack.pushPose();
            baseModel.body.translateAndRotate(poseStack);
            if (isBaby) {
                poseStack.scale(state.ageScale, state.ageScale, state.ageScale);
                if (jetpack) {
                    poseStack.translate(0, -0.05, 0);
                }
            } else if (jetpack) {
                poseStack.translate(0, 0.3, 0);
            } else {
                poseStack.translate(0, 0.41, 0);
            }
            if (jetpack) {
                poseStack.translate(0, 0, 0.145);
                poseStack.scale(1.02F, -1.02F, -1.02F);
            } else {
                poseStack.translate(0, 0, 0.28);
                poseStack.scale(1, -1, -1);
            }
            bodyItem.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}