package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.render.GLSMHelper;
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
        GLSMHelper.INSTANCE.rotateZ(160, 1);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
        GLSMHelper.INSTANCE.translateY(-1.0F).rotateY(135, 1).rotateZ(-20, 1);

        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                GLSMHelper.INSTANCE.rotateY(55, 1);
            } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                GLSMHelper.INSTANCE.rotateY(-160, 1).rotateX(30, 1);
            } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                GLSMHelper.INSTANCE.translateY(0.7F).rotateY(75, 1);
            } else {//if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                GLSMHelper.INSTANCE.translateXY(-0.5F, 0.7F);
            }
            GLSMHelper.INSTANCE.scale(2.5F).translateYZ(-1.0F, -0.5F);
        } else if (transformType == TransformType.GUI) {
            GLSMHelper.INSTANCE.translateX(-0.6F).rotateY(45, 1);
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