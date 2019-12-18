package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelResistiveHeater;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderResistiveHeaterItem extends MekanismItemStackRenderer {

    private static ModelResistiveHeater resistiveHeater = new ModelResistiveHeater();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.translatef(0.05F, -0.96F, 0.05F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "resistive_heater.png"));
        resistiveHeater.render(0.0625F, ItemDataUtils.getDouble(stack, "energyStored") > 0, Minecraft.getInstance().textureManager, true);*/
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