package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;

public interface ICustomArmor {

    default <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          STATE state, ItemStack stack) {
        //If the model isn't meant to be shown don't bother rendering anything
        if (isVisible(baseModel, state)) {
            poseStack.pushPose();
            baseModel.setupAnim(state);
            baseModel.root().translateAndRotate(poseStack);
            //Similar to HumanoidArmorLayer, we don't want to rescale for small armor stands as they just have their overall scale changed
            boolean isBaby = state.isBaby && state.entityType != EntityTypes.ARMOR_STAND;
            render(baseModel, poseStack, nodeCollector, lightCoords, state, isBaby, stack);
            poseStack.popPose();
        }
    }

    <STATE extends HumanoidRenderState> boolean isVisible(HumanoidModel<STATE> baseModel, STATE state);

    <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, STATE state,
          boolean isBaby, ItemStack stack);
}