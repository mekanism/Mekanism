package mekanism.client.render.item.gear;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.render.MekanismRenderHelper;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderScubaTank extends MekanismItemStackRenderer {

    private static ModelScubaTank scubaTank = new ModelScubaTank();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {
        MekanismRenderHelper renderHelper = new MekanismRenderHelper(true).rotateZ(180, 1).rotateY(90, -1).scale(1.6F).translateXY(0.2F, -0.5F);
        MekanismRenderer.bindTexture(MekanismUtils.getResource(ResourceType.RENDER, "ScubaSet.png"));
        scubaTank.render(0.0625F);
        renderHelper.cleanup();
    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}