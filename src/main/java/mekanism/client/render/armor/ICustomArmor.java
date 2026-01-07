package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ICustomArmor {

    <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, STATE state, ItemStack stack);
}