package mekanism.client.render.item.gear;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderFlameThrower extends MekanismItemStackRenderer {

    private static ModelFlamethrower flamethrower = new ModelFlamethrower();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight,
          TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight,
          TransformType transformType) {
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(160));
        matrix.func_227861_a_(0, -1, 0);
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(135));
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(-20));

        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(55));
            } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-160));
                matrix.func_227863_a_(Vector3f.field_229179_b_.func_229187_a_(30));
            } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                matrix.func_227861_a_(0, 0.7, 0);
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(75));
            } else {//if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                matrix.func_227861_a_(-0.5, 0.7, 0);
            }
            matrix.func_227862_a_(2.5F, 2.5F, 2.5F);
            matrix.func_227861_a_(0, -1, -0.5);
        } else if (transformType == TransformType.GUI) {
            matrix.func_227861_a_(-0.6, 0, 0);
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(45));
        }
        flamethrower.render(matrix, renderer, light, otherLight);
        matrix.func_227865_b_();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}