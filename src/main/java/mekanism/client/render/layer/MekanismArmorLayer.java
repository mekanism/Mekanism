package mekanism.client.render.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.ParametersAreNonnullByDefault;
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

@ParametersAreNonnullByDefault
public class MekanismArmorLayer<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

    public MekanismArmorLayer(IEntityRenderer<T, M> entityRenderer, A modelLeggings, A modelArmor) {
        super(entityRenderer, modelLeggings, modelArmor);
    }

    @Override
    public void render(MatrixStack matrix, IRenderTypeBuffer renderer, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTick,
          float ageInTicks, float netHeadYaw, float headPitch) {
        renderArmorPart(matrix, renderer, entity, EquipmentSlotType.CHEST, packedLightIn);
        renderArmorPart(matrix, renderer, entity, EquipmentSlotType.LEGS, packedLightIn);
        renderArmorPart(matrix, renderer, entity, EquipmentSlotType.FEET, packedLightIn);
        renderArmorPart(matrix, renderer, entity, EquipmentSlotType.HEAD, packedLightIn);
    }

    private void renderArmorPart(MatrixStack matrix, IRenderTypeBuffer renderer, T entity, EquipmentSlotType slot, int light) {
        ItemStack stack = entity.getItemStackFromSlot(slot);
        Item item = stack.getItem();
        if (item instanceof ISpecialGear && item instanceof ArmorItem) {
            ArmorItem armorItem = (ArmorItem) item;
            if (armorItem.getEquipmentSlot() == slot) {
                CustomArmor model = ((ISpecialGear) item).getGearModel();
                getEntityModel().setModelAttributes((BipedModel<T>) model);
                setModelSlotVisible((A) model, slot);
                model.render(matrix, renderer, light, OverlayTexture.NO_OVERLAY, stack.hasEffect(), entity, stack);
            }
        }
    }
}