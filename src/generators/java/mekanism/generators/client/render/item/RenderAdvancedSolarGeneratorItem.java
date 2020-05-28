package mekanism.generators.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.generators.client.model.ModelAdvancedSolarGenerator;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderAdvancedSolarGeneratorItem extends MekanismItemStackRenderer {

    private static final ModelAdvancedSolarGenerator advancedSolarGenerator = new ModelAdvancedSolarGenerator();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        //TODO: Adjust this some, especially in third person we can make this look better
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        matrix.translate(0, 0.2F, 0);
        //Shrink the size of the model
        matrix.scale(0.352F, 0.352F, 0.352F);
        advancedSolarGenerator.render(matrix, renderer, light, overlayLight);
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