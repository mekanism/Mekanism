package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFreeRunners extends MekanismItemStackRenderer {

    private static ModelFreeRunners freeRunners = new ModelFreeRunners();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType, MekanismRenderHelper renderHelper) {
        MekanismRenderHelper localRenderHelper = new MekanismRenderHelper(true);
        GlStateManager.rotate(180, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(90, 0.0F, -1.0F, 0.0F);
        localRenderHelper.scale(2.0F).translate(0.2F, -1.43F, 0.12F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "FreeRunners.png"));
        freeRunners.render(0.0625F);
        localRenderHelper.cleanup();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}