package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFlamethrower;
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
public class RenderFlameThrower extends MekanismItemStackRenderer {

    private static ModelFlamethrower flamethrower = new ModelFlamethrower();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(160, 0, 0, 1);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
        GlStateManager.translate(0, -1.0F, 0);
        GlStateManager.rotate(135, 0, 1, 0);
        GlStateManager.rotate(-20, 0, 0, 1);

        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                GlStateManager.rotate(55, 0, 1, 0);
            } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                GlStateManager.rotate(-160, 0, 1, 0);
                GlStateManager.rotate(30, 1, 0, 0);
            } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                GlStateManager.translate(0, 0.7F, 0);
                GlStateManager.rotate(75, 0, 1, 0);
            } else {//if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                GlStateManager.translate(-0.5F, 0.7F, 0);
            }
            GlStateManager.scale(2.5F, 2.5F, 2.5F);
            GlStateManager.translate(0, -1.0F, -0.5F);
        } else if (transformType == TransformType.GUI) {
            GlStateManager.translate(-0.6F, 0, 0);
            GlStateManager.rotate(45, 0, 1, 0);
        }
        flamethrower.render(0.0625F);
        GlStateManager.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}