package mekanism.client.render;

import mekanism.client.render.armor.ICustomArmor;

//TODO - 26.1 curio render
public record MekanismCurioRenderer(ICustomArmor model) {} /*implements ICurioRenderer {

    @Override
    public <T extends EntityRenderState, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
          RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount,
          float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel) {
            this.model.render(humanoidModel, matrixStack, renderTypeBuffer, light, OverlayTexture.NO_OVERLAY, partialTicks, stack.hasFoil(), slotContext.entity(), stack);
        }
    }
}*/
