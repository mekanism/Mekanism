package mekanism.client.render.item.basicblock;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
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
public class RenderSecurityDeskItem {
    private static ModelSecurityDesk securityDesk = new ModelSecurityDesk();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            GlStateManager.rotate(90, 0.0F, 1.0F, 0.0F);
        } else {
            GlStateManager.rotate(-90, 0.0F, 1.0F, 0.0F);
        }
        GlStateManager.scale(0.8F, 0.8F, 0.8F);
        GlStateManager.translate(0.0F, -0.8F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk.png"));
        securityDesk.render(0.0625F, Minecraft.getMinecraft().renderEngine);
    }
}