package mekanism.client.render.armor;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public abstract class CustomArmor extends BipedModel<LivingEntity> {

    protected CustomArmor(float size) {
        super(size);
    }

    public abstract void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, ItemStack stack);
}