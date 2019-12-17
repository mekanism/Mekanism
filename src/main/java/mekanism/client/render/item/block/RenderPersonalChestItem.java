package mekanism.client.render.item.block;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class RenderPersonalChestItem extends MekanismItemStackRenderer {

    //TODO: 1.15
    //private static ChestModel personalChest = new ChestModel();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(180, 0, 1, 0);
        GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
        GlStateManager.translatef(0, 1.0F, 1.0F);
        GlStateManager.scalef(1.0F, -1F, -1F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "personal_chest.png"));
        personalChest.renderAll();
        GlStateManager.popMatrix();
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