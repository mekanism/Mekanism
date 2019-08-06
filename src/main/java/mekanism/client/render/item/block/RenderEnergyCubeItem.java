package mekanism.client.render.item.block;

import javax.annotation.Nonnull;
import mekanism.client.MekanismClient;
import mekanism.client.model.ModelEnergyCube;
import mekanism.client.model.ModelEnergyCube.ModelEnergyCore;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.GlowInfo;
import mekanism.client.render.item.ItemLayerWrapper;
import mekanism.client.render.item.MekanismItemStackRenderer;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.common.SideData.IOState;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
        EnergyCubeTier tier = ((ItemBlockEnergyCube) stack.getItem()).getTier(stack);
        if (tier == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -1.0F, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);
        energyCube.render(0.0625F, tier, Minecraft.getMinecraft().renderEngine, true);

        for (Direction side : Direction.values()) {
            MekanismRenderer.bindTexture(RenderEnergyCube.baseTexture);
            energyCube.renderSide(0.0625F, side, side == Direction.NORTH ? IOState.OUTPUT : IOState.INPUT, tier, Minecraft.getMinecraft().renderEngine);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();

        double energy = ItemDataUtils.getDouble(stack, "energyStored");

        if (energy / tier.getMaxEnergy() > 0.1) {
            GlStateManager.pushMatrix();
            MekanismRenderer.bindTexture(RenderEnergyCube.coreTexture);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlowInfo glowInfo = MekanismRenderer.enableGlow();

            //Begin core color
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
            MekanismRenderer.color(tier.getBaseTier());
            GlStateManager.translate(0, (float) Math.sin(Math.toRadians(3 * MekanismClient.ticksPassed)) / 7, 0);
            GlStateManager.rotate(4 * MekanismClient.ticksPassed, 0, 1, 0);
            GlStateManager.rotate(36F + 4 * MekanismClient.ticksPassed, 0, 1, 1);
            core.render(0.0625F);
            MekanismRenderer.resetColor();
            GlStateManager.popMatrix();
            //End core color

            MekanismRenderer.disableGlow(glowInfo);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
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