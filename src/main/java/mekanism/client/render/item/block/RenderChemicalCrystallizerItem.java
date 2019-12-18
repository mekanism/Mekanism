package mekanism.client.render.item.block;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nonnull;
import mekanism.client.model.ModelChemicalCrystallizer;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderChemicalCrystallizerItem extends MekanismItemStackRenderer {

    private static ModelChemicalCrystallizer chemicalCrystallizer = new ModelChemicalCrystallizer();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.translatef(0.05F, -1.001F, 0.05F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "chemical_crystallizer.png"));
        //TODO: 1.15
        //chemicalCrystallizer.render(0.0625F);
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