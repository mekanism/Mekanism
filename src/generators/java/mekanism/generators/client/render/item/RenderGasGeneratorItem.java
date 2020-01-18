package mekanism.generators.client.render.item;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelGasGenerator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderGasGeneratorItem extends MekanismItemStackRenderer {

    private static ModelGasGenerator gasGenerator = new ModelGasGenerator();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotatef(180, 0, 1, 1);
        GlStateManager.rotatef(90, -1, 0, 0);
        GlStateManager.translatef(0, -1.0F, 0);
        GlStateManager.rotatef(180, 0, 1, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "gas_burning_generator.png"));
        gasGenerator.render(0.0625F);
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