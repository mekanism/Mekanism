package mekanism.generators.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.generators.client.model.ModelGasGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderGasGeneratorItem extends MekanismItemStackRenderer {

    private static ModelGasGenerator gasGenerator = new ModelGasGenerator();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.func_227863_a_(Vector3f.field_229183_f_.func_229187_a_(180));
        matrix.func_227861_a_(0, -1, 0);
        matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(90));
        gasGenerator.render(matrix, renderer, light, overlayLight);
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