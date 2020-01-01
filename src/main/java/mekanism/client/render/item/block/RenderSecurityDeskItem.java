package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderSecurityDeskItem extends MekanismItemStackRenderer {

    private static ModelSecurityDesk securityDesk = new ModelSecurityDesk();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
        } else {
            matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(-90));
        }
        matrix.func_227862_a_(0.8F, 0.8F, 0.8F);
        matrix.func_227861_a_(0, -0.8, 0);
        securityDesk.render(matrix, renderer, light, overlayLight);
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}