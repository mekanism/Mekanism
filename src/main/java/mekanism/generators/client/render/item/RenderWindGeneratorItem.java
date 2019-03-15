package mekanism.generators.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.client.model.ModelWindGenerator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWindGeneratorItem {

    private static ModelWindGenerator windGenerator = new ModelWindGenerator();
    private static int angle = 0;

    public static void renderStack(@Nonnull ItemStack stack) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.4F, 0.0F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "WindGenerator.png"));
        //TODO: Only update angle if the player is not in a blacklisted dimension, one that has no "wind".
        //The best way to do this would be to add an event listener for dimension change.
        //The event is server side only so we would need to send a packet to clients to tell them if they are
        //in a blacklisted dimension or not.
        angle = (angle + 2) % 360;
        windGenerator.render(0.016F, angle);
        GlStateManager.popMatrix();
    }
}