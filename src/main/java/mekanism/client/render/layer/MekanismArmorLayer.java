package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@ParametersAreNotNullByDefault
public class MekanismArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, M, A> {

    public MekanismArmorLayer(RenderLayerParent<T, M> entityRenderer, HumanoidArmorLayer<T, M, A> vanillaLayer, ModelManager manager) {
        super(entityRenderer, vanillaLayer.innerModel, vanillaLayer.outerModel, manager);
    }

    @Override
    public void render(PoseStack matrix, MultiBufferSource renderer, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks,
          float ageInTicks, float netHeadYaw, float headPitch) {
        renderArmorPart(matrix, renderer, entity, EquipmentSlot.CHEST, packedLightIn, partialTicks);
        renderArmorPart(matrix, renderer, entity, EquipmentSlot.LEGS, packedLightIn, partialTicks);
        renderArmorPart(matrix, renderer, entity, EquipmentSlot.FEET, packedLightIn, partialTicks);
        renderArmorPart(matrix, renderer, entity, EquipmentSlot.HEAD, packedLightIn, partialTicks);
    }

    private void renderArmorPart(PoseStack matrix, MultiBufferSource renderer, T entity, EquipmentSlot slot, int light, float partialTicks) {
        ItemStack stack = entity.getItemBySlot(slot);
        Item item = stack.getItem();
        if (item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot && IClientItemExtensions.of(item) instanceof ISpecialGear specialGear) {
            ICustomArmor model = specialGear.gearModel();
            A coreModel = slot == EquipmentSlot.LEGS ? innerModel : outerModel;
            getParentModel().copyPropertiesTo(coreModel);
            setPartVisibility(coreModel, slot);
            model.render(coreModel, matrix, renderer, light, OverlayTexture.NO_OVERLAY, partialTicks, stack.hasFoil(), entity, stack);
        }
    }
}