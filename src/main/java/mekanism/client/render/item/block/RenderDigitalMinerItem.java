package mekanism.client.render.item.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderDigitalMinerItem extends MekanismItemStackRenderer {

    private static final ModelDigitalMiner digitalMiner = new ModelDigitalMiner();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180));
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            matrix.rotate(Vector3f.YP.rotationDegrees(-90));
        } else if (transformType != TransformType.GUI) {
            matrix.rotate(Vector3f.YP.rotationDegrees(90));
        }
        matrix.translate(0.35, 0.1, 0);
        //Scale the model to the correct size
        matrix.scale(0.352F, 0.352F, 0.352F);
        digitalMiner.render(matrix, renderer, light, overlayLight, !StorageUtils.getStoredEnergyFromNBT(stack).isZero());
        matrix.pop();
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