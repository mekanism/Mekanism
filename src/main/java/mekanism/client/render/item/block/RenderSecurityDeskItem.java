package mekanism.client.render.item.block;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderSecurityDeskItem extends MekanismItemStackRenderer {

    private static ModelSecurityDesk securityDesk = new ModelSecurityDesk();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotatef(180, 1, 0, 0);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            GlStateManager.rotatef(90, 0, 1, 0);
        } else {
            GlStateManager.rotatef(-90, 0, 1, 0);
        }
        GlStateManager.translatef(0.8F, 0.8F, 0.8F);
        GlStateManager.translatef(0, -0.8F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "security_desk.png"));
        securityDesk.render(0.0625F, Minecraft.getInstance().textureManager);
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}