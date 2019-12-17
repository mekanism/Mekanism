package mekanism.client.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.RenderState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

//TODO: Make some item models that just have straight edges be gotten via JSON instead of using a TEISR
// Maybe we can even clean up some of the things currently using TESRs instead of using json.
// For example the normal solar panel block
public abstract class MekanismItemStackRenderer extends ItemStackTileEntityRenderer {

    protected abstract void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType);

    protected abstract void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType);

    @Nonnull
    protected abstract TransformType getTransform(@Nonnull ItemStack stack);

    protected boolean earlyExit() {
        return false;
    }

    protected void renderWithTransform(@Nonnull ItemStack stack) {
        TransformType transformType = getTransform(stack);
        if (transformType == TransformType.GUI) {
            GlStateManager.rotatef(180, 0, 1, 0);
        }

        renderBlockSpecific(stack, transformType);

        if (!earlyExit()) {
            if (transformType == TransformType.GUI) {
                GlStateManager.rotatef(90, 0, 1, 0);
            } else {
                GlStateManager.rotatef(180, 0, 1, 0);
            }
            renderItemSpecific(stack, transformType);
        }
    }

    @Override
    public void func_228364_a_(@Nonnull ItemStack stack, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int otherLight) {
        Tessellator tessellator = Tessellator.getInstance();
        RenderState renderState = MekanismRenderer.pauseRenderer(tessellator);
        GlStateManager.pushMatrix();
        GlStateManager.translatef(0.5F, 0.5F, 0.5F);
        GlStateManager.rotatef(180, 0, 1, 0);

        renderWithTransform(stack);

        GlStateManager.popMatrix();
        MekanismRenderer.resumeRenderer(tessellator, renderState);
    }
}