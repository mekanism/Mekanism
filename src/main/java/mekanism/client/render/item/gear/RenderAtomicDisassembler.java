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
    private static ModelAtomicDisassembler atomicDisassembler = new ModelAtomicDisassembler();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.func_227860_a_();
        matrix.func_227862_a_(1.4F, 1.4F, 1.4F);
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-90));
            }
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(45));
            matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(50));
            matrix.func_227862_a_(2, 2, 2);
            matrix.func_227861_a_(0, -0.4, 0.4);
        } else if (transformType == TransformType.GUI) {
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(225));
            matrix.func_227863_a_(guiVec.func_229187_a_(45));
            matrix.func_227862_a_(0.6F, 0.6F, 0.6F);
            matrix.func_227861_a_(0, -0.2, 0);
        } else {
            if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
            }
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(45));
            matrix.func_227861_a_(0, -0.7, 0);
        }
        atomicDisassembler.render(matrix, renderer, light, overlayLight);
        matrix.func_227865_b_();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}