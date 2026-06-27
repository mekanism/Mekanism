package mekanism.client.integration.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.common.util.StackUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public record MekanismCurioRenderer(ICustomArmor model) implements ICurioRenderer {

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
          SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context,
          float yRotation, float xRotation) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (StackUtils.isRenderableArmor(equippable) && renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            render(humanoidModel, matrixStack, submitNodeCollector, packedLight, renderState, stack);
        }
    }

    private <STATE extends HumanoidRenderState> void render(HumanoidModel<STATE> baseModel, PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightCoords,
          LivingEntityRenderState state, ItemStack stack) {
        this.model.render(baseModel, poseStack, nodeCollector, lightCoords, (STATE) state, stack);
    }
}
