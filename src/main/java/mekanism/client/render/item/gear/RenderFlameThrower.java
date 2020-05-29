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

    private static final ModelFlamethrower flamethrower = new ModelFlamethrower();
    public static ItemLayerWrapper model;

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(160));
        matrix.translate(0, -1, 0);
        matrix.rotate(Vector3f.YP.rotationDegrees(135));
        matrix.rotate(Vector3f.ZP.rotationDegrees(-20));

        if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_RIGHT_HAND
            || transformType == TransformType.FIRST_PERSON_LEFT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                matrix.rotate(Vector3f.YP.rotationDegrees(55));
            } else if (transformType == TransformType.FIRST_PERSON_LEFT_HAND) {
                matrix.rotate(Vector3f.YP.rotationDegrees(-160));
                matrix.rotate(Vector3f.XP.rotationDegrees(30));
            } else if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND) {
                matrix.translate(0, 0.7, 0);
                matrix.rotate(Vector3f.YP.rotationDegrees(75));
            } else {//if(type == TransformType.THIRD_PERSON_LEFT_HAND)
                matrix.translate(-0.5, 0.7, 0);
            }
            matrix.scale(2.5F, 2.5F, 2.5F);
            matrix.translate(0, -1, -0.5);
        } else if (transformType == TransformType.GUI) {
            matrix.translate(-0.6, 0, 0);
            matrix.rotate(Vector3f.YP.rotationDegrees(45));
        }
        flamethrower.render(matrix, renderer, light, overlayLight);
        matrix.pop();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}