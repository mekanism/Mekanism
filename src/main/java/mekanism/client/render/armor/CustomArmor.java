package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

//TODO - 1.18: Evaluate
public abstract class CustomArmor {//extends HumanoidModel<LivingEntity> {

    protected CustomArmor() {//ModelPart root) {
        //super(root);
    }

    public abstract void render(HumanoidModel<? extends LivingEntity> baseModel, @Nonnull PoseStack matrix, @Nonnull MultiBufferSource renderer,
          int light, int overlayLight, float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack);
}