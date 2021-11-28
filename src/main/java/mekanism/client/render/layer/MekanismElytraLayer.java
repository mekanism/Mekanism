package mekanism.client.render.layer;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MekanismElytraLayer<T extends LivingEntity, M extends BipedModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation HDPE_ELYTRA = Mekanism.rl("textures/entity/hdpe_elytra.png");

    public MekanismElytraLayer(IEntityRenderer<T, M> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public boolean shouldRender(@Nonnull ItemStack stack, @Nonnull T entity) {
        return stack.getItem() == MekanismItems.HDPE_REINFORCED_ELYTRA.getItem();
    }

    @Nonnull
    @Override
    public ResourceLocation getElytraTexture(@Nonnull ItemStack stack, @Nonnull T entity) {
        return HDPE_ELYTRA;
    }
}