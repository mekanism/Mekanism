package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFlameThrower extends MekanismItemStackRenderer {

    private static ModelFlamethrower flamethrower = new ModelFlamethrower();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
        MekanismRenderHelper localRenderHelper = new MekanismRenderHelper(true).rotateZ(160, 1);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "Flamethrower.png"));
        localRenderHelper.translateY(-1.0F).rotateY(135, 1).rotateZ(-20, 1);

        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                localRenderHelper.rotateY(55, 1);
            } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                localRenderHelper.rotateY(-160, 1).rotateX(30, 1);
            } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                localRenderHelper.translateY(0.7F).rotateY(75, 1);
            } else {//if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                localRenderHelper.translateXY(-0.5F, 0.7F);
            }
            localRenderHelper.scale(2.5F);
            localRenderHelper.translateYZ(-1.0F, -0.5F);
        } else if (transformType == TransformType.GUI) {
            localRenderHelper.translateX(-0.6F).rotateY(45, 1);
        }

        flamethrower.render(0.0625F);
        localRenderHelper.cleanup();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}