package mekanism.client.render.item.gear;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderScubaTank extends MekanismItemStackRenderer {

    private static ModelScubaTank scubaTank = new ModelScubaTank();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.rotatef(90, 0, -1, 0);
        RenderSystem.scalef(1.6F, 1.6F, 1.6F);
        RenderSystem.translatef(0.2F, -0.5F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "scuba_set.png"));
        //TODO: 1.15
        //scubaTank.render(0.0625F);
        RenderSystem.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}