package mekanism.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.armor.CustomArmor;
import mekanism.common.item.interfaces.ISpecialGear;
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
    protected void renderArmorPart(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, T entity, float limbSwing, float limbSwingAmount, float partialTick,
          float ageInTicks, float netHeadYaw, float headPitch, @Nonnull EquipmentSlotType slot, int light) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        Item item = stack.getItem();
        if (item instanceof ISpecialGear && item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) item;
            if (armorItem.getEquipmentSlot() == slot) {
                CustomArmor model = ((ISpecialGear) item).getGearModel();
                getEntityModel().setModelAttributes((BipedModel<T>) model);
                model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTick);
                setModelSlotVisible((A) model, slot);
                model.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                model.render(matrix, renderer, light, OverlayTexture.NO_OVERLAY, stack.hasEffect());
            }
        }
    }

    @Nonnull
    @Override
    public A getModelFromSlot(EquipmentSlotType slot) {
        return slot == EquipmentSlotType.LEGS ? this.modelLeggings : this.modelArmor;
    }
}