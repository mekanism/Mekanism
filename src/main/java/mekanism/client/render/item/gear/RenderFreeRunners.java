package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFreeRunners extends MekanismItemStackRenderer {

    private static ModelFreeRunners freeRunners = new ModelFreeRunners();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.translate(0.2F, -1.43F, 0.12F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
        freeRunners.render(0.0625F);
        GlStateManager.popMatrix();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}