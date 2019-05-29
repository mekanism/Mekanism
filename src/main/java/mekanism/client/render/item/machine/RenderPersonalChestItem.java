package mekanism.client.render.item.machine;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderHelper;
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

    public static void renderStack(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
        MekanismRenderHelper localRenderHelper = new MekanismRenderHelper(true);
        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        localRenderHelper.translateAll(-0.5F).translateYZ(1.0F, 1.0F).scale(1.0F, -1F, -1F);

        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "PersonalChest.png"));

        personalChest.renderAll();
        localRenderHelper.cleanup();
    }
}