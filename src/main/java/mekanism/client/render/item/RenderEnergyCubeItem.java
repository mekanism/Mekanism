package mekanism.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.SideData.IOState;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.ITierItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnergyCubeItem extends MekanismItemStackRenderer {

    private static ModelEnergyCube energyCube = new ModelEnergyCube();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack) {
        GlStateManager.pushMatrix();
        EnergyCubeTier tier = EnergyCubeTier.values()[((ITierItem) stack.getItem()).getBaseTier(stack).ordinal()];
        MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);

        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(180F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.0F, 0.0F);

        MekanismRenderer.blendOn();

        energyCube.render(0.0625F, tier, Minecraft.getMinecraft().renderEngine, true);

        for (EnumFacing side : EnumFacing.VALUES) {
            MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);
            energyCube.renderSide(0.0625F, side, side == EnumFacing.NORTH ? IOState.OUTPUT : IOState.INPUT, tier,
                  Minecraft.getMinecraft().renderEngine);
        }

        MekanismRenderer.blendOff();
        GlStateManager.popMatrix();
    }

    @Override
    protected void renderItemSpecific(@Nonnull ItemStack stack, TransformType transformType) {

    }

    @Nonnull
    @Override
    protected TransformType getTransform(@Nonnull ItemStack stack) {
        return model.getTransform();
    }
}