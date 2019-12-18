package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.generators.client.model.ModelHeatGenerator;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderHeatGeneratorItem extends MekanismItemStackRenderer {

    private static ModelHeatGenerator heatGenerator = new ModelHeatGenerator();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.translatef(0, -1.0F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "heat_generator.png"));
        heatGenerator.render(0.0625F, ItemDataUtils.getDouble(stack, "energyStored") > 0, Minecraft.getInstance().textureManager);*/
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