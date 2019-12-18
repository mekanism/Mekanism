package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderPersonalChestItem extends MekanismItemStackRenderer {

    //TODO: 1.15
    //private static ChestModel personalChest = new ChestModel();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.rotatef(180, 0, 1, 0);
        RenderSystem.translatef(-0.5F, -0.5F, -0.5F);
        RenderSystem.translatef(0, 1.0F, 1.0F);
        RenderSystem.scalef(1.0F, -1F, -1F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "personal_chest.png"));
        personalChest.renderAll();
        RenderSystem.popMatrix();*/
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}