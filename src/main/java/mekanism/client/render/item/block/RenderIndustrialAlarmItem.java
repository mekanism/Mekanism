package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderIndustrialAlarmItem extends MekanismItemStackRenderer {

    private static ModelIndustrialAlarm industrialAlarm = new ModelIndustrialAlarm();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.rotate(Vector3f.YP.rotationDegrees(180));
        }
        matrix.scale(1.5F, 1.5F, 1.5F);
        matrix.translate(0, -0.2, 0);
        industrialAlarm.render(matrix, renderer, light, overlayLight, false, 0, true);
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