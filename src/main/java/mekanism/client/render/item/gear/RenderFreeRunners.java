package mekanism.client.render.item.gear;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderFreeRunners extends MekanismItemStackRenderer {

    private static ModelFreeRunners freeRunners = new ModelFreeRunners();
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
        matrix.func_227862_a_(2.0F, 2.0F, 2.0F);
        matrix.func_227861_a_(0.2, -1.43, 0.12);
        freeRunners.render(matrix, renderer, light, overlayLight, stack.hasEffect());
        matrix.func_227865_b_();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}