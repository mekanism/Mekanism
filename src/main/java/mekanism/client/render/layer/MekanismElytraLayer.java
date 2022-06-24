package mekanism.client.render.layer;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MekanismElytraLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends ElytraLayer<T, M> {

    private static final ResourceLocation HDPE_ELYTRA = Mekanism.rl("textures/entity/hdpe_elytra.png");

    public MekanismElytraLayer(RenderLayerParent<T, M> entityRenderer, EntityModelSet modelSet) {
        super(entityRenderer, modelSet);
    }

    @Override
    public boolean shouldRender(@NotNull ItemStack stack, @NotNull T entity) {
        return stack.getItem() == MekanismItems.HDPE_REINFORCED_ELYTRA.asItem();
    }

    @NotNull
    @Override
    public ResourceLocation getElytraTexture(@NotNull ItemStack stack, @NotNull T entity) {
        return HDPE_ELYTRA;
    }
}