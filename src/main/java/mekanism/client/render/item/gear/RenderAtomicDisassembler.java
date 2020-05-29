package mekanism.client.render.item.gear;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderAtomicDisassembler extends MekanismItemStackRenderer {

    private static final Vector3f guiVec = new Vector3f(-MekanismUtils.ONE_OVER_ROOT_TWO, 0, -MekanismUtils.ONE_OVER_ROOT_TWO);
    private static final ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.push();
        matrix.scale(1.4F, 1.4F, 1.4F);
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                matrix.rotate(Vector3f.YP.rotationDegrees(-90));
            }
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
            matrix.rotate(Vector3f.XP.rotationDegrees(50));
            matrix.scale(2, 2, 2);
            matrix.translate(0, -0.4, 0.4);
        } else if (transformType == TransformType.GUI) {
            matrix.rotate(Vector3f.YP.rotationDegrees(225));
            matrix.rotate(guiVec.rotationDegrees(45));
            matrix.scale(0.6F, 0.6F, 0.6F);
            matrix.translate(0, -0.2, 0);
        } else {
            if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                matrix.rotate(Vector3f.YP.rotationDegrees(90));
            }
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
            matrix.translate(0, -0.7, 0);
        }
        atomicDisassembler.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}