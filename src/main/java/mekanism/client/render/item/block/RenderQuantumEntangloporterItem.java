package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;

public class RenderQuantumEntangloporterItem extends MekanismItemStackRenderer {

    private static ModelQuantumEntangloporter quantumEntangloporter = new ModelQuantumEntangloporter();
    public static ItemLayerWrapper model;

    @Override
    public void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        //TODO: 1.15
        /*RenderSystem.rotatef(180, 0, 0, 1);
        RenderSystem.translatef(0, -1.0F, 0);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "quantum_entangloporter.png"));
        quantumEntangloporter.render(0.0625F, Minecraft.getInstance().textureManager, true);*/
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