package mekanism.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.RenderState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class MekanismItemStackRenderer extends TileEntityItemStackRenderer {

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
            GlStateManager.rotate(180, 0, 1, 0);
        }

        renderBlockSpecific(stack, transformType);

        if (!earlyExit()) {
            if (transformType == TransformType.GUI) {
                GlStateManager.rotate(90, 0, 1, 0);
            } else {
                GlStateManager.rotate(180, 0, 1, 0);
            }
            renderItemSpecific(stack, transformType);
        }
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack) {
        Tessellator tessellator = Tessellator.getInstance();
        RenderState renderState = MekanismRenderer.pauseRenderer(tessellator);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        GlStateManager.rotate(180, 0, 1, 0);

        renderWithTransform(stack);

        GlStateManager.popMatrix();
        MekanismRenderer.resumeRenderer(tessellator, renderState);
    }
}