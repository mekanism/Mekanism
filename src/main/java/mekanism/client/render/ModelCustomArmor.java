package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;

public abstract class ModelCustomArmor extends BipedModel<LivingEntity> {

    protected ModelCustomArmor(float size) {
        super(size);
    }

    public abstract void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect);
}