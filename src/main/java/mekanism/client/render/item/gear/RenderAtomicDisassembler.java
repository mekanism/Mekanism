package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAtomicDisassembler extends MekanismItemStackRenderer {

    private static ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.4F, 1.4F, 1.4F);
        GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);

        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND
              || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                GlStateManager.rotate(-90, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(50, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0.0F, -0.4F, 0.4F);
        } else if (transformType == TransformType.GUI) {
            GlStateManager.rotate(225, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(45, -1.0F, 0.0F, -1.0F);
            GlStateManager.scale(0.6F, 0.6F, 0.6F);
            GlStateManager.translate(0.0F, -0.2F, 0.0F);
        } else {
            if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.7F, 0.0F);
        }

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "AtomicDisassembler.png"));
        atomicDisassembler.render(0.0625F);
        GlStateManager.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}