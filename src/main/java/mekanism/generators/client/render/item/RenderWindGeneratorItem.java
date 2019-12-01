package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindGeneratorItem {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;
    private static float lastTicksUpdated = 0;

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180, 0, 0, 1);
        if (transformType == TransformType.THIRD_PERSON_RIGHT_HAND || transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.translate(0, 0.4F, 0);
            if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
                GlStateManager.rotate(-45, 0, 1, 0);
            } else {
                GlStateManager.rotate(45, 0, 1, 0);
            }
            GlStateManager.rotate(50, 1, 0, 0);
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            GlStateManager.translate(0, -0.4F, 0);
        } else {
            if (transformType == TransformType.GUI) {
                GlStateManager.rotate(90, 0, 1, 0);
            } else if (transformType == TransformType.FIRST_PERSON_RIGHT_HAND) {
                GlStateManager.rotate(180, 0, 1, 0);
            }
            GlStateManager.translate(0, 0.4F, 0);
        }

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
        //TODO: Only update angle if the player is not in a blacklisted dimension, one that has no "wind".
        //The best way to do this would be to add an event listener for dimension change.
        //The event is server side only so we would need to send a packet to clients to tell them if they are
        //in a blacklisted dimension or not.
        if (lastTicksUpdated != Minecraft.getMinecraft().getRenderPartialTicks()) {
            angle = (angle + 2) % 360;
            lastTicksUpdated = Minecraft.getMinecraft().getRenderPartialTicks();
        }
        windGenerator.render(0.016F, angle);
        GlStateManager.popMatrix();
    }
}