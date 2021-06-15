package mekanism.client.render.layer;

import javax.annotation.Nonnull;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
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
        if (stack.getItem() == MekanismItems.HDPE_REINFORCED_ELYTRA.getItem()) {
            return true;
        }
        if (stack.getItem() instanceof ItemMekaSuitArmor && MekanismAPI.getModuleHelper().isEnabled(stack, MekanismModules.ELYTRA_UNIT)) {
            //Only have the elytra render on the mekasuit when in flight
            //TODO - 10.1: Re-evaluate if we want this to be the case or not once we figure out exactly how the models and stuff will be
            return entity.isFallFlying();
        }
        return false;
    }

    //TODO - 10.1: Uncomment once we have textures, for now just use vanilla's texture
    /*@Nonnull
    @Override
    public ResourceLocation getElytraTexture(@Nonnull ItemStack stack, @Nonnull T entity) {
        //TODO - 10.1: Have a separate texture for HDPE elytra and the elytra unit
        // Maybe even make the elytra unit have a custom layer with its own rendering logic
        return HDPE_ELYTRA;
    }*/
}