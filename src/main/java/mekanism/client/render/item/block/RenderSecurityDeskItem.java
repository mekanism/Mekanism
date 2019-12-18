package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderSecurityDeskItem extends MekanismItemStackRenderer {

    private static ModelSecurityDesk securityDesk = new ModelSecurityDesk();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.rotatef(180, 1, 0, 0);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            RenderSystem.rotatef(90, 0, 1, 0);
        } else {
            RenderSystem.rotatef(-90, 0, 1, 0);
        }
        RenderSystem.scalef(0.8F, 0.8F, 0.8F);
        RenderSystem.translatef(0, -0.8F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "security_desk.png"));
        securityDesk.render(0.0625F, Minecraft.getInstance().textureManager);*/
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