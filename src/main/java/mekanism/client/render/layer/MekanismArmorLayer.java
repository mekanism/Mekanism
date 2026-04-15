package mekanism.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@ParametersAreNotNullByDefault
public class MekanismArmorLayer<STATE extends HumanoidRenderState, MODEL extends HumanoidModel<STATE>, A extends HumanoidModel<STATE>> extends HumanoidArmorLayer<STATE, MODEL, A> {

    public MekanismArmorLayer(RenderLayerParent<STATE, MODEL> entityRenderer, HumanoidArmorLayer<STATE, MODEL, A> vanillaLayer, EquipmentLayerRenderer equipmentRenderer) {
        super(entityRenderer, vanillaLayer.modelSet, vanillaLayer.babyModelSet, equipmentRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords, STATE state, float yRot, float xRot) {
        renderArmorPart(poseStack, nodeCollector, state.chestEquipment, EquipmentSlot.CHEST, state, lightCoords);
        renderArmorPart(poseStack, nodeCollector, state.legsEquipment, EquipmentSlot.LEGS, state, lightCoords);
        renderArmorPart(poseStack, nodeCollector, state.feetEquipment, EquipmentSlot.FEET, state, lightCoords);
        renderArmorPart(poseStack, nodeCollector, state.headEquipment, EquipmentSlot.HEAD, state, lightCoords);
    }

    //copied from super private method
    private A getArmorModel(STATE state, EquipmentSlot slot) {
        return (state.isBaby ? this.babyModelSet : this.modelSet).get(slot);
    }
    
    //PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ItemStack itemStack, EquipmentSlot slot, int lightCoords, S state
    private void renderArmorPart(PoseStack poseStack, SubmitNodeCollector nodeCollector, ItemStack stack, EquipmentSlot slot, STATE state, int lightCoords) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        //TODO - 1.21.11: Should we check there is an asset id like super does?
        if (equippable != null && equippable.slot() == slot && IClientItemExtensions.of(stack.getItem()) instanceof ISpecialGear specialGear) {
            ICustomArmor model = specialGear.gearModel();
            //TODO - 1.21.11: Fix this as it seems baby models now get handled via this
            //TODO - 1.21.11: Figure out baby vs not
            A coreModel = getArmorModel(state, slot);
            model.render(coreModel, poseStack, nodeCollector, lightCoords, state, stack);
        }
    }
}