package mekanism.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

//TODO: Make some item models that just have straight edges be gotten via JSON instead of using a TEISR
// Maybe we can even clean up some of the things currently using TESRs instead of using json.
// For example the normal solar panel block
//TODO: Declare the transformations via json and that they are "built in renderers", instead of messing around with the ItemLayerWrapper stuff?
@Deprecated
public abstract class MekanismItemStackRenderer extends ItemStackTileEntityRenderer {

    protected void renderItemSpecific(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight,
          TransformType transformType) {
    }

    @Nonnull
    protected abstract TransformType getTransform(@Nonnull ItemStack stack);

    @Override
    public void render(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight) {
        matrix.push();
        matrix.translate(0.5, 0.5, 0.5);
        TransformType transformType = getTransform(stack);
        if (transformType == TransformType.GUI) {
            matrix.rotate(Vector3f.YP.rotationDegrees(90));
        }
        renderItemSpecific(stack, matrix, renderer, light, overlayLight, transformType);
        matrix.pop();
    }
}