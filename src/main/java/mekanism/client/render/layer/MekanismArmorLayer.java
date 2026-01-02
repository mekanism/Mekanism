package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@ParametersAreNotNullByDefault
public class MekanismArmorLayer<STATE extends HumanoidRenderState, MODEL extends HumanoidModel<STATE>, A extends HumanoidModel<STATE>> extends HumanoidArmorLayer<STATE, MODEL, A> {

    public MekanismArmorLayer(RenderLayerParent<STATE, MODEL> entityRenderer, HumanoidArmorLayer<STATE, MODEL, A> vanillaLayer, EquipmentLayerRenderer equipmentRenderer) {
        super(entityRenderer, vanillaLayer.modelSet, vanillaLayer.babyModelSet, equipmentRenderer);
    }

    @Override
    public void submit(PoseStack matrix, SubmitNodeCollector collector, int packedLight, STATE state, float yRot, float xRot) {
        renderArmorPart(matrix, collector, state, state.chestEquipment, EquipmentSlot.CHEST, packedLight, partialTicks);
        renderArmorPart(matrix, collector, state, state.legsEquipment, EquipmentSlot.LEGS, packedLight, partialTicks);
        renderArmorPart(matrix, collector, state, state.feetEquipment, EquipmentSlot.FEET, packedLight, partialTicks);
        renderArmorPart(matrix, collector, state, state.headEquipment, EquipmentSlot.HEAD, packedLight, partialTicks);
    }

    private void renderArmorPart(PoseStack matrix, MultiBufferSource renderer, STATE state, ItemStack stack, EquipmentSlot slot, int light, float partialTicks) {
        Item item = stack.getItem();
        if (item instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot && IClientItemExtensions.of(item) instanceof ISpecialGear specialGear) {
            ICustomArmor model = specialGear.gearModel();
            //TODO - 1.21.11: Fix this as it seems baby models now get handled via this
            A coreModel = slot == EquipmentSlot.LEGS ? innerModel : outerModel;
            getParentModel().copyPropertiesTo(coreModel);
            setPartVisibility(coreModel, slot);
            model.render(coreModel, matrix, renderer, light, OverlayTexture.NO_OVERLAY, partialTicks, stack.hasFoil(), state, stack);
        }
    }
}