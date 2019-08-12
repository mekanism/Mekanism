package mekanism.client.render.item.gear;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderFreeRunners extends MekanismItemStackRenderer {

    private static ModelFreeRunners freeRunners = new ModelFreeRunners();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.rotatef(180, 0, 0, 1);
        GlStateManager.rotatef(90, 0, -1, 0);
        GlStateManager.translatef(2.0F, 2.0F, 2.0F);
        GlStateManager.translatef(0.2F, -1.43F, 0.12F);
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