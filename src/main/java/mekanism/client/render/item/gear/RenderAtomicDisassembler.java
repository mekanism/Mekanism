package mekanism.client.render.item.gear;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderAtomicDisassembler extends MekanismItemStackRenderer {

    private static ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.4F, 1.4F, 1.4F);
        RenderSystem.rotatef(180, 0, 0, 1);

        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                RenderSystem.rotatef(-90, 0, 1, 0);
            }
            RenderSystem.rotatef(45, 0, 1, 0);
            RenderSystem.rotatef(50, 1, 0, 0);
            RenderSystem.scalef(2.0F, 2.0F, 2.0F);
            RenderSystem.translatef(0, -0.4F, 0.4F);
        } else if (transformType == TransformType.GUI) {
            RenderSystem.rotatef(225, 0, 1, 0);
            RenderSystem.rotatef(45, -1, 0, -1);
            RenderSystem.scalef(0.6F, 0.6F, 0.6F);
            RenderSystem.translatef(0, -0.2F, 0);
        } else {
            if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                RenderSystem.rotatef(90, 0, 1, 0);
            }
            RenderSystem.rotatef(45, 0, 1, 0);
            RenderSystem.translatef(0, -0.7F, 0);
        }

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "atomic_disassembler.png"));
        //TODO: 1.15
        //atomicDisassembler.render(0.0625F);
        RenderSystem.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}