package mekanism.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.RenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
            GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        }

        renderBlockSpecific(stack, transformType);

        if (!earlyExit()) {
            if (transformType == TransformType.GUI) {
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
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
        GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);

        //
        renderWithTransform(stack);
        //

        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        GlStateManager.enableCull();//TODO: This line was not in generators does it matter
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.popMatrix();

        MekanismRenderer.resumeRenderer(tessellator, renderState);
    }
}