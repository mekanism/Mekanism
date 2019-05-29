package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelDigitalMiner;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderDigitalMinerItem {

    private static ModelDigitalMiner digitalMiner = new ModelDigitalMiner();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 0, 1);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            GlStateManager.rotate(-90, 0, 1, 0);
        } else if (transformType != TransformType.GUI) {
            GlStateManager.rotate(90, 0, 1, 0);
        }
        renderHelper.translateXY(0.35, 0.1);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "DigitalMiner.png"));
        digitalMiner.render(0.022F, ItemDataUtils.getDouble(stack, "energyStored") > 0, Minecraft.getMinecraft().renderEngine, true);
        GlStateManager.popMatrix();
    }
}