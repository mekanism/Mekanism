package mekanism.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderHelper;
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

    protected abstract void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper);

    protected abstract void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper);

    @Nonnull
    protected abstract TransformType getTransform(@Nonnull ItemStack stack);

    protected boolean earlyExit() {
        return false;
    }

    protected void renderWithTransform(@Nonnull ItemStack stack, MekanismRenderHelper renderHelper) {
        TransformType transformType = getTransform(stack);
        if (transformType == TransformType.GUI) {
            GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        }

        renderBlockSpecific(stack, transformType, renderHelper);

        if (!earlyExit()) {
            if (transformType == TransformType.GUI) {
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }
            renderItemSpecific(stack, transformType, renderHelper);
        }
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack) {
        Tessellator tessellator = Tessellator.getInstance();
        RenderState renderState = MekanismRenderer.pauseRenderer(tessellator);

        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).translateAll(0.5F);
        GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);

        //
        renderWithTransform(stack, renderHelper);
        //

        //TODO: Make this use helper for lighting and then disable it after bindTexture?
        renderHelper.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);
        renderHelper.enableCull();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        renderHelper.cleanup();

        MekanismRenderer.resumeRenderer(tessellator, renderState);
    }
}