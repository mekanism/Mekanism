package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPersonalChestItem {

    private static ModelChest personalChest = new ModelChest();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();

        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        GlStateManager.translate(0, 1.0F, 1.0F);
        GlStateManager.scale(1.0F, -1F, -1F);

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

        personalChest.renderAll();
        GlStateManager.popMatrix();
    }
}