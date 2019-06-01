package mekanism.client.render.item.basicblock;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelSecurityDesk;
import mekanism.client.render.GLSMHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSecurityDeskItem {

    private static ModelSecurityDesk securityDesk = new ModelSecurityDesk();

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GLSMHelper.INSTANCE.rotateX(180, 1);
        if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            GLSMHelper.INSTANCE.rotateY(90, 1);
        } else {
            GLSMHelper.INSTANCE.rotateY(-90, 1);
        }
        GLSMHelper.INSTANCE.scale(0.8F).translateY(-0.8F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "SecurityDesk.png"));
        securityDesk.render(0.0625F, Minecraft.getMinecraft().renderEngine);
    }
}