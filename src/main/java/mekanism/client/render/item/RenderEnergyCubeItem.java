package mekanism.client.render.item;

import javax.annotation.Nonnull;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.SideData.IOState;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.base.ITierItem;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderEnergyCubeItem extends MekanismItemStackRenderer {

    private static ModelEnergyCube energyCube = new ModelEnergyCube();
    private static ModelEnergyCore core = new ModelEnergyCore();
    public static ItemLayerWrapper model;

    @Override
    protected void renderBlockSpecific(@Nonnull ItemStack stack, TransformType transformType) {
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

        double energy = ItemDataUtils.getDouble(stack, "energyStored");

        if (energy / tier.maxEnergy > 0.1) {
            GlStateManager.pushMatrix();
            MekanismRenderer.bindTexture(RenderEnergyCube.coreTexture);

            MekanismRenderer.blendOn();
            MekanismRenderer.glowOn();

            int[] c = RenderEnergyCube.COLORS[tier.getBaseTier().ordinal()];

            GlStateManager.pushMatrix();
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
            GL11.glColor4f((float) c[0] / 255F, (float) c[1] / 255F, (float) c[2] / 255F,
                  (float) (energy / tier.maxEnergy));
            GlStateManager.translate(0, (float) Math.sin(Math.toRadians(MekanismClient.ticksPassed * 3)) / 7, 0);
            GlStateManager.rotate(MekanismClient.ticksPassed * 4, 0, 1, 0);
            GlStateManager.rotate(36F + MekanismClient.ticksPassed * 4, 0, 1, 1);
            core.render(0.0625F);
            MekanismRenderer.resetColor();
            GlStateManager.popMatrix();

            MekanismRenderer.glowOff();
            MekanismRenderer.blendOff();

            GlStateManager.popMatrix();
        }
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