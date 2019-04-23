package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelQuantumEntangloporter;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderQuantumEntangloporterItem {
    private static ModelQuantumEntangloporter quantumEntangloporter = new ModelQuantumEntangloporter();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.0F, -1.0F, 0.0F);
        MekanismRenderer
              .bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "QuantumEntangloporter.png"));
        quantumEntangloporter.render(0.0625F, Minecraft.getMinecraft().renderEngine, true);
    }
}