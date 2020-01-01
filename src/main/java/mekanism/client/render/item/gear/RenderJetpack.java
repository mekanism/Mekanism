package mekanism.client.render.item.gear;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelJetpack;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderJetpack extends MekanismItemStackRenderer {

    private static ModelJetpack jetpack = new ModelJetpack();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.func_227860_a_();
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        matrix.func_227863_a_(Vector3f.field_229180_c_.func_229187_a_(90));
        matrix.func_227861_a_(0.2, -0.35, 0);
        jetpack.render(matrix, renderer, light, overlayLight);
        matrix.func_227865_b_();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}