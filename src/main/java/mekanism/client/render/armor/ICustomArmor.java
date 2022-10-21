package mekanism.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ICustomArmor {

    void render(HumanoidModel<? extends LivingEntity> baseModel, @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight,
          float partialTicks, boolean hasEffect, LivingEntity entity, ItemStack stack);
}