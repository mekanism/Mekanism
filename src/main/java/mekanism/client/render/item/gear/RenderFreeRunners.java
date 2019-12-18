package mekanism.client.render.item.gear;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderFreeRunners extends MekanismItemStackRenderer {

    private static ModelFreeRunners freeRunners = new ModelFreeRunners();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.rotatef(90, 0, -1, 0);
        RenderSystem.scalef(2.0F, 2.0F, 2.0F);
        RenderSystem.translatef(0.2F, -1.43F, 0.12F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "free_runners.png"));
        //TODO: 1.15
        //freeRunners.render(0.0625F);
        RenderSystem.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}