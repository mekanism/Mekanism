package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderDigitalMinerItem extends MekanismItemStackRenderer {

    private static ModelDigitalMiner digitalMiner = new ModelDigitalMiner();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.pushMatrix();
        RenderSystem.rotatef(180, 0, 0, 1);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            RenderSystem.rotatef(-90, 0, 1, 0);
        } else if (transformType != TransformType.GUI) {
            RenderSystem.rotatef(90, 0, 1, 0);
        }
        RenderSystem.translatef(0.35F, 0.1F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "digital_miner.png"));
        digitalMiner.render(0.022F, ItemDataUtils.getDouble(stack, "energyStored") > 0, Minecraft.getInstance().textureManager, true);
        RenderSystem.popMatrix();*/
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