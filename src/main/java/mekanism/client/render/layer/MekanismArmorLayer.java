package mekanism.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.armor.CustomArmor;
import mekanism.common.item.gear.ISpecialGear;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MekanismArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    public MekanismArmorLayer(IEntityRenderer<T, M> entityRenderer, A modelLeggings, A modelArmor) {
        super(entityRenderer, modelLeggings, modelArmor);
    }

    @Override
    public void func_225628_a_(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, T entity, float limbSwing, float limbSwingAmount,
          float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        func_229129_a_(matrix, renderer, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.CHEST, light);
        func_229129_a_(matrix, renderer, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.LEGS, light);
        func_229129_a_(matrix, renderer, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.FEET, light);
        func_229129_a_(matrix, renderer, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch, EquipmentSlotType.HEAD, light);
    }

    //TODO: Once there are mappings/the bot supports returning this maybe try to just AT this instead of having to also copy the above method.
    private void func_229129_a_(MatrixStack matrix, IRenderTypeBuffer renderer, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
          float netHeadYaw, float headPitch, EquipmentSlotType slot, int light) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        Item item = stack.getItem();
        if (item instanceof ISpecialGear && item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) item;
            if (armorItem.getEquipmentSlot() == slot) {
                CustomArmor model = ((ISpecialGear) item).getGearModel();
                getEntityModel().func_217148_a((BipedModel<T>) model);
                model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTick);
                setModelSlotVisible((A) model, slot);
                model.func_225597_a_(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                renderArmor(matrix, renderer, light, stack.hasEffect(), model);
            }
        }
    }

    private void renderArmor(MatrixStack matrix, IRenderTypeBuffer renderer, int light, boolean hasEffect, CustomArmor model) {
        model.render(matrix, renderer, light, OverlayTexture.field_229196_a_, hasEffect);
    }

    @Nonnull
    @Override
    public A func_215337_a(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.LEGS ? this.modelLeggings : this.modelArmor;
    }
}